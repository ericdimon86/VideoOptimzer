
package com.att.aro.core.videoanalysis.impl;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.att.aro.core.AROConfig;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.util.IStringParse;
import com.att.aro.core.util.StringParse;
import com.att.aro.core.videoanalysis.parsers.DashEncodedSegmentParser;
import com.att.aro.core.videoanalysis.parsers.DashParser;
import com.att.aro.core.videoanalysis.parsers.DashSegmentTimelineParser;
import com.att.aro.core.videoanalysis.parsers.XmlManifestHelper;
import com.att.aro.core.videoanalysis.parsers.encodedsegment.MPDEncodedSegment;
import com.att.aro.core.videoanalysis.parsers.encodedsegment.RepresentationESL;
import com.att.aro.core.videoanalysis.parsers.segmenttimeline.AdaptationSetTL;
import com.att.aro.core.videoanalysis.parsers.segmenttimeline.MPDSegmentTimeline;
import com.att.aro.core.videoanalysis.parsers.segmenttimeline.RepresentationST;
import com.att.aro.core.videoanalysis.parsers.segmenttimeline.SegmentST;
import com.att.aro.core.videoanalysis.parsers.segmenttimeline.SegmentTemplateST;
import com.att.aro.core.videoanalysis.parsers.smoothstreaming.SSM;
import com.att.aro.core.videoanalysis.pojo.ChildManifest;
import com.att.aro.core.videoanalysis.pojo.Manifest;
import com.att.aro.core.videoanalysis.pojo.ManifestCollection;
import com.att.aro.core.videoanalysis.pojo.VideoEvent.VideoType;
import com.att.aro.core.videoanalysis.pojo.VideoFormat;

public class ManifestBuilderDASH extends ManifestBuilder {

	protected static final Logger LOG = LogManager.getLogger(ManifestBuilderDASH.class.getName());
	protected static final Pattern pattern = Pattern.compile("^(#[A-Z0-9\\-]*)");

	DashParser parseDash;

	ApplicationContext context = new AnnotationConfigApplicationContext(AROConfig.class);
	IStringParse stringParse = context.getBean(IStringParse.class);

	private MPDEncodedSegment mpdOut;
	@SuppressWarnings("unused")
	private SSM ssmOut;
	@SuppressWarnings("unused")
	private MPDSegmentTimeline mpdSegmentTimeline;
	private Double lastPresentationTimeOffset;
	private boolean manifestLiveUpdate;
	private boolean isDynamic;

	public ManifestBuilderDASH() {

	}

	public String buildSegmentKey(String segmentFile) {
		String key = segmentFile;
		if (manifestCollection.getCommonBaseLength() > 0) {
			key = segmentFile.substring(0, manifestCollection.getCommonBaseLength());
		} else {
			int pos = segmentFile.lastIndexOf(".");
			if (pos > -1) {
				key = segmentFile.substring(0, pos);
			}
		}
		return key;
	}

	/**
	 * Locate and return a segment number.
	 * 
	 * @param request
	 * @return segment number or -1 if not found
	 */
	public SegmentInfo getSegmentInfo(HttpRequestResponseInfo request) {
		ManifestCollection manifestCollection;
		SegmentInfo segmentInfo = null;

		String key = buildKey(request);
		manifestCollection = findManifest(request);
		if (manifestCollection != null) {
			segmentInfo = manifestCollection.getSegmentTrie().get(key);
		}
		return segmentInfo;
	}

	/**
	 * Locate and return a ChildManifest.
	 * 
	 * @param request
	 * @return segment number or -1 if not found
	 */
	public ChildManifest getChildManifest(HttpRequestResponseInfo request) {
		ManifestCollection manifestCollection = findManifest(request);
		return manifestCollection.getSegmentChildManifestTrie().get(buildKey(request));
	}

	public void parseManifestData(Manifest newManifest, byte[] data) {
		if (data == null || data.length == 0) {
			return;
		}
		String strData = new String(data);
		String[] sData = (new String(data)).split("\r\n");
		if (sData == null || sData.length == 1) {
			sData = (new String(data)).split("[\n\r]");
		}

		XmlManifestHelper manifestView = new XmlManifestHelper(data);
		String key = newManifest.getVideoName() != null ? newManifest.getVideoName() : "null";
		newManifest.setVideoFormat(VideoFormat.MPEG4);

		if (strData.contains("type=\"dynamic\"")){
			isDynamic = true;
		} else {
			isDynamic = false;
		}

		if (strData.contains("<SegmentTimeline>")) {
			newManifest.setVideoType(VideoType.DASH_SEGMENTTIMELINE);
			mpdSegmentTimeline = (MPDSegmentTimeline) manifestView.getManifest();

			DashSegmentTimelineParser parseDashdynamic = new DashSegmentTimelineParser(mpdSegmentTimeline, newManifest, manifestCollection, childManifest);
			String contentType = "video";
			AdaptationSetTL adaptationSet = parseDashdynamic.findAdaptationSet(contentType);
			SegmentTemplateST segmentTemplate = adaptationSet.getSegmentTemplate();
			String initialization = segmentTemplate.getInitialization(); // segment 0 'moov'
			String media = segmentTemplate.getMedia(); // segment x 'moof'
			Double presentationTimeOffset = StringParse.stringToDouble(segmentTemplate.getPresentationTimeOffset(), 0);
			Double timescale = StringParse.stringToDouble(segmentTemplate.getTimescale(), 0);

			List<SegmentST> segmentList = segmentTemplate.getSegmentTimeline().getSegmentList();
			LOG.info(segmentTemplate.getSegmentTimeline());
			
			List<RepresentationST> representationList = adaptationSet.getRepresentation();
			
			if (isDynamic && (lastPresentationTimeOffset == null || !lastPresentationTimeOffset.equals(presentationTimeOffset))) {
				lastPresentationTimeOffset = presentationTimeOffset;
				switchManifestCollection(newManifest, key, manifest.getRequestTime());
				manifestLiveUpdate = false;
			} else if (isDynamic) {
				manifestLiveUpdate = isDynamic;
				LOG.info("update manifest");
			} else {
				switchManifestCollection(newManifest, key, manifest.getRequestTime());
				manifestLiveUpdate = false;
			}
			
			double timeCursor = 0;
			Integer qualityID = 0;
			for (RepresentationST representation : adaptationSet.getRepresentation()) {
				int segmentID = 0;
				qualityID++;
				String rid = representation.getContentID();
				if (manifestLiveUpdate) {
					String cKey = media.replaceAll("\\$(RepresentationID)\\$", rid).replaceAll("\\$(Time)\\$", "(\\\\d+)");
					if ((childManifest = manifestCollection.getUriNameChildMap().get(cKey)) == null) {
						LOG.error("failed to find childmanifest :" + cKey);
						continue;
					}
					PatriciaTrie<SegmentInfo> segmentInfoList = null;
					segmentInfoList = childManifest.getSegmentList();
					for (String segmentKey : segmentInfoList.keySet()) {
						SegmentInfo segmentInfo = segmentInfoList.get(segmentKey);
						int sid = segmentInfo.getSegmentID();
						if (segmentID < sid) {
							segmentID = sid + 1;
							timeCursor = segmentInfo.getStartTime();
						}
					}
					
				} else {
					// segment 0 (moov)

					// segment 1-end (moof) a regex for all
					childManifest = createChildManifest(newManifest, "", media.replaceAll("\\$(RepresentationID)\\$", rid).replaceAll("\\$(Time)\\$", "(\\\\d+)"));
					childManifest.setPixelHeight(StringParse.stringToDouble(representation.getHeight(), 0).intValue());
					childManifest.setPixelWidth(StringParse.stringToDouble(representation.getWidth(), 0).intValue());

					childManifest.setBandwidth(StringParse.stringToDouble(representation.getBandwidth(), 0));
					childManifest.setQuality(qualityID);
				}

				String segmentUriName;
				SegmentInfo segmentInfo;
				
				for (SegmentST segment : segmentList) {
					Double timePos = StringParse.stringToDouble(segment.getStartTime(), 0);
					Double duration = StringParse.stringToDouble(segment.getDuration(), 0);
					Double repetition = StringParse.stringToDouble(segment.getRepeat(), 0);
					
					for (int idx = 0; idx <= repetition; idx++) {
						timePos += duration;
						if (timePos <= timeCursor) {
							continue;
						}
						segmentInfo = new SegmentInfo();
						segmentInfo.setStartTime(timePos);
						segmentInfo.setDuration(duration / timescale);
						segmentInfo.setQuality(qualityID.toString());
						segmentInfo.setVideo("video".equals(contentType));
						segmentInfo.setSegmentID(segmentID++);
						if (idx == 0) {
							segmentUriName = initialization.replaceAll("\\$(.*)\\$", rid);
							addToSegmentManifestCollectionMap(segmentUriName);
						} else {
							segmentUriName = media.replaceAll("\\$(RepresentationID)\\$", rid).replaceAll("\\$(Time)\\$", String.format("%.0f", timePos));
						}
						LOG.info(idx + ": " + segmentUriName + " : " + segmentInfo);

						childManifest.addSegment(segmentUriName, segmentInfo);
					}
				}
				addToSegmentManifestCollectionMap(childManifest.getUriName());
			}

			String id = representationList.get(2).getContentID();
			String segmentUri = initialization.replaceAll("\\$(.*)\\$", id);
			segmentUri = media.replaceAll("\\$(RepresentationID)\\$", id).replaceAll("\\$(Time)\\$", segmentTemplate.getPresentationTimeOffset());
			LOG.info(segmentUri);

			LOG.info(formatKey("/Content/DASH.abre/Live/channel(AandEHD-1382.dfw.1080)/1540537551937item-3item_init.m4i"));
			LOG.info(formatKey("/Content/DASH.abre/Live/channel(AandEHD-1382.dfw.1080)/1540537551937item-3item_Segment-77578141974675.m4v"));

			LOG.info(String.format("presentationTimeOffset %.0f", presentationTimeOffset));

			if (getChildManifest() == null) {
				childManifest = createChildManifest(newManifest, "", newManifest.getUriStr());
			}

		} else {
			switchManifestCollection(newManifest, key, manifest.getRequestTime());
			newManifest.setVideoType(VideoType.DASH_ENCODEDSEGMENTLIST);
			mpdOut = (MPDEncodedSegment) manifestView.getManifest();
			parseDash = new DashEncodedSegmentParser(mpdOut, newManifest, manifestCollection, childManifest);
			String contentType = "video";
			List<RepresentationESL> representationList = ((DashEncodedSegmentParser) parseDash).getRepresentationAmz(contentType);

			SortedMap<Double, RepresentationESL> sortedRepresentationAmz = sortRepresentationByBandwidth(representationList);

			Integer qualityID = 0;
			for (RepresentationESL representation : sortedRepresentationAmz.values()) {
				if (representation.getEncodedSegment() != null) {
					qualityID++;
					LOG.info(String.format("representation.getBandwidth() %d:%s", qualityID, representation.getBandwidth()));
					generateChildManifestFromEncodedSegmentList(newManifest, contentType, qualityID, representation);
					addToSegmentManifestCollectionMap(childManifest.getUriName());
				}
			}
		}
		LOG.info("import Manifest:\n" + strData);
	}

	public void addToSegmentManifestCollectionMap(String segmentUriName) {
		segmentManifestCollectionMap.put(
				segmentUriName + "|" + manifestCollection.getManifest().getVideoName()
				, manifestCollection.getManifest().getVideoName()
			);
	}

	public void generateChildManifestFromEncodedSegmentList(Manifest newManifest, String contentType, Integer qualityID, RepresentationESL representation) {
		childManifest = createChildManifest(newManifest, "", representation.getBaseURL());
		childManifest.setBandwidth(StringParse.stringToDouble(representation.getBandwidth(), 0));
		childManifest.setCodecs(representation.getCodecs());
		childManifest.setQuality(qualityID);
		childManifest.setPixelHeight(StringParse.stringToDouble(representation.getHeight(), 0).intValue());
		childManifest.setPixelWidth(StringParse.stringToDouble(representation.getWidth(), 0).intValue());

		Double duration = StringParse.stringToDouble(representation.getEncodedSegment().getDuration(), 0);
		Double timescale = StringParse.stringToDouble(representation.getEncodedSegment().getTimescale(), 0);
		String[] encodedSegments = representation.getEncodedSegment().getEncodedSegmentListValue().split(";");

		childManifest.setSegmentCount(encodedSegments.length);
		childManifest.setVideo("video".equals(contentType));

		int idx = 0;
		for (String encodedSegmentElement : encodedSegments) {
			if (encodedSegmentElement == null) {
				break;
			}
			SegmentInfo segmentInfo = new SegmentInfo();
			segmentInfo.setSegmentID(idx);
			segmentInfo.setVideo(childManifest.isVideo());
			segmentInfo.setSize(calcSizeFromEncodedSegmentElement(encodedSegmentElement));
			segmentInfo.setDuration(duration / timescale);
			segmentInfo.setQuality(qualityID.toString());

			childManifest.addSegment(encodedSegmentElement, segmentInfo);

			idx++;
		}
	}

	/**
	 * <pre>
	 * Calculate size from EncodedSegmentList element
	 * 
	 * @param segment
	 * @return int size
	 */
	public int calcSizeFromEncodedSegmentElement(String encodedSegmentElement) {
		try {
			Long beginByte = Long.valueOf(encodedSegmentElement.substring(0, 16), 16);
			Long endByte = Long.valueOf(encodedSegmentElement.substring(17, 33), 16);
			int size = (int) (endByte - beginByte);
			return size;
		} catch (NumberFormatException e) {
			LOG.error("failed to convert Hex to Long :", e);
			return 0;
		}
	}

	/**
	 * <pre>
	 * Do not assume that bandwidth will always be in order Make sure it is in sorted order by Bandwidth so that qualityID can be assigned 100000, 150000, 200000,
	 * 300000, 500000, 800000, 1200000, 1800000
	 */
	public SortedMap<Double, RepresentationESL> sortRepresentationByBandwidth(List<RepresentationESL> representationList) {
		SortedMap<Double, RepresentationESL> sortedRepresentationAmz = new TreeMap<>();
		for (RepresentationESL representation : representationList) {
			Double bandwidth = StringParse.stringToDouble(representation.getBandwidth());
			sortedRepresentationAmz.put(bandwidth, representation);
		}
		return sortedRepresentationAmz;
	}

	@Override
	protected ChildManifest createChildManifest(Manifest manifest, String parameters, String childUriName) {
		ChildManifest childManifest = new ChildManifest();
		childManifest.setManifest(manifest);
		childManifest.setUriName(childUriName);

		childUriName = childUriName.replaceAll("%2f", "/");
		manifestCollection.addToUriNameChildMap(StringUtils.countMatches(childUriName, "/"), childUriName, childManifest);
		return childManifest;
	}

	@Override
	public String toString() {
		StringBuilder strblr = new StringBuilder("\n\tManifestBuilderDASH :");
		strblr.append(super.toString());
		return strblr.toString();
	}

	@Override
	public String buildSegmentName(HttpRequestResponseInfo request, String extension) {
		String name = request.getObjNameWithoutParams();
		int dot = name.lastIndexOf('.');
		if (dot > -1) {
			int sep = name.substring(0, dot).lastIndexOf('/');
			if (sep > -1) {
				name = name.substring(sep + 1);
				dot = name.lastIndexOf('.');
			}
		}
		name = StringUtils.replace(name, "/", "-");
		if (dot == -1) {
			dot = name.length();
		}
		name = StringUtils.replace(name.substring(0, dot), ".", "-");
		return name + extension;
	}

}
