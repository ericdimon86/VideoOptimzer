package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.packetreader.pojo.Packet;

public class MinificationImplTest extends BaseTest {

	MinificationImpl MinificationImpl;
	Packet packet;
	PacketAnalyzerResult tracedata;

	private TraceDirectoryResult dirdata;

	AbstractBestPracticeResult result = null;

	@Before
	public void setup() {
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		dirdata = Mockito.mock(TraceDirectoryResult.class);
	}

	/**
	 * tests with empty session
	 */
	@Test
	public void runTest_1() {

		List<Session> sessionlist;
		Session session_1;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		MinificationImpl = (MinificationImpl) context.getBean("minify");
		result = MinificationImpl.runTest(tracedata);

		assertEquals(
				"Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.",
				result.getAboutText());
		assertEquals("Minify CSS, JS and HTML", result.getDetailTitle());
		assertEquals("File Download: Minify CSS, JS and HTML", result.getOverviewTitle());
		assertEquals("Your trace passes.", result.getResultText());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("PASS", result.getResultType().toString());

	}

	/**
	 * tests html compression
	 */
	@Test
	public void runTest_2() {
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;
		// Session session_2;

		session_1 = mock(Session.class);
		// session_2 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);
		// sessionlist.add(session_2);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);

		Mockito.when((TraceDirectoryResult) tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		// Mockito.when(session_2.getRequestResponseInfo()).thenReturn(reqList_2);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);
		// Mockito.when(req_1.getContentType()).thenReturn("text/javascript");

		MinificationImpl = (MinificationImpl) context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(rr_2.getContentType()).thenReturn("text/html"); // "application/ecmascript"
																		// "application/json"
																		// "application/javascript"
																		// "text/javascript"
																		// "message/http"
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = "GET /b?s=792600146&_R=&_L=m%06refresh-announcement%01l%06refresh-banner-dismiss%02refresh-banner-line1%02refresh-banner-line1%02refresh-banner-whats-new-cta%08m%06global-nav%01l%06Logo-main%02Signup-main%02Explore-main%02Explore-recent_photos%02Explore-the_commons%02Explore-getty_collection%02Explore-galleries%02Explore-world_map%02Explore-app_garden%02Explore-camera_finder%02Explore-flickr_blog%02Upload-main%02Account-sign_in%08m%06photo-container%01l%06action-menu-click%02%23%02%23comments%02share-menu-click%02prev_button%02lightbox%02next_button%08m%06comments%01l%06%2Fphotos%2F%2F%02%2Fhtml.gne%3Ftighten%3D0%26type%3Dcomment&t=1370377426&_P=2.9.4%05A_pn%03%2Fphoto.gne%04A_sid%03QhIPpAes0Fti%04_w%03www.flickr.com%2Fphotos%2F92457242%40N04%2F8404052962%2F%04A_%031 HTTP/1.1\r\nHost: geo.yahoo.com\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/536.29.13 (KHTML, like Gecko) Version/6.0.4 Safari/536.29.13\r\nAccept: */*\r\nReferer: http://www.flickr.com/photos/92457242@N04/8404052962/\r\nAccept"
					+ "-Language: en-us\r\nAccept-Encoding: gzip, deflate\r\nCookie: ucs=bnas=0; B=actnnip8iv3a6&b=3&s=4l\r\nConnection: keep-alive\r\n\r\n"
					+ "                                                                                                                                                                               "
					+ "HTTP/1.1 200 OK\r\nDate: Tue, 04 Jun 2013 20:23:44 GMT\r\nP3P: policyref=\"http://info.yahoo.com/w3c/p3p.xml\", CP=\"CAO DSP COR CUR ADM DEV TAI PSA PSD IVAi IVDi CONi TELo OTPi OUR DELi SAMi OTRi UNRi PUBi IND PHY ONL UNI PUR FIN COM NAV INT DEM CNT STA POL HEA PRE LOC GOV\"\r\nCache-Control: no-cache, no-store, private\r\nPragma: no-cache\r\nContent-Length: 43\r\nConnection: close\r\nContent-Type: image/gif\r\n\r\nGIF89a  ��  ������   !��    ,       D ;";

			Mockito.when(reqhelper.isJavaScript("text/html")).thenReturn(false);
			Mockito.when(reqhelper.isHtml("text/html")).thenReturn(true);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");

		} catch (Exception e) {
			e.printStackTrace();
		}

		result = MinificationImpl.runTest(tracedata);

		assertEquals(
				"Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.",
				result.getAboutText());
		assertEquals("Minify CSS, JS and HTML", result.getDetailTitle());
		assertEquals("File Download: Minify CSS, JS and HTML", result.getOverviewTitle());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());

	}

	/**
	 * tests css compression
	 */
	@Test
	public void runTest_3() {
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);

		Mockito.when((TraceDirectoryResult) tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);

		MinificationImpl = (MinificationImpl) context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentType()).thenReturn("text/css");
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = "GET /style.css HTTP/1.1\nHost: searchinsidevideo.com\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/536.29.13 (KHTML, like Gecko) Version/6.0.4 Safari/536.29.13"
					+ "\nAccept: text/css,*/*;q=0.1"
					+ "\nReferer: http://searchinsidevideo.com/\nAccept-Language: en-us\nAccept-Encoding: gzip, deflate\nConnection: keep-alive\n\nHTTP/1.1 200 OK\nDate: Tue, 04 Jun 2013 20:24:38 GMT\nServer: Apache\nLast-Modified: Fri, 28 Dec 2012 06:49:57 GMT\nETag: \"161827b-122c-4d1e41252af40\"\nAccept-Ranges: bytes\nContent-Length: 4652\nConnection: close\nContent-Type: text/css\n\n@charset \"UTF-8\";\r\n\r\nbody,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,\r\nform,fieldset,input,textarea,p,blockquote,th,td{\r\npadding:0;\r\nmargin:0;\r\n}\r\n\r\ntable{\r\nborder-collapse: collapse;\r\nborder-spacing:0;\r\n}\r\nimg{\r\nborder:0;\r\nline-height:0;\r\n}\r\nol,ul{\r\nlist-style:none;\r\n}\r\n\r\nbody{\r\nfont:12px/1.5 \"������������\",\"Meiryo\",arial,\"������������������ Pro W3\",\"Hiragino Kaku Gothic Pro\",Osaka,\"������ ���������������\",\"MS PGothic\",Sans-Serif;\r\ncolor:#333;\r\n}\r\n\r\n\r\na:link,a:visited{color:#438918;text-decoration:none;}\r\na:hover{color:#367f93;}\r\na:active, a:focus {outline:0;}\r\nimg{border:0;}\r\n\r\n\r\n\r\n/*******************************\r\n���������������\r\n*******************************/\r\n#header, #mainNav, #wrapper,#footer ul{\r\nmargin:0 auto;\r\nwidth:880px;\r\nclear:both;\r\n}\r\n\r\n#sidebar{\r\nfloat:left;\r\nwidth:233px;\r\npadding:22px 0 50px;\r\n}\r\n\r\n#main{\r\nfloat:right;\r\nwidth:627px;\r\npadding:22px 0 50px;\r\n}\r\n\r\n\r\n/*******************************\r\n/* ������������\r\n*******************************/\r\n#headerWrap{\r\nheight:147px;\r\nbackground:#fff url(images/wall.jpg) repeat-x 0 0;\r\n}\r\n\r\n#header{\r\nposition:relative;\r\nheight:147px;\r\n}\r\n\r\n#header h1,#header h2,#header p{\r\nposition:absolute;\r\ntop:31px;\r\nfont-size:10px;\r\nfont-weight:normal;\r\nline-height:22px;\r\n}\r\n\r\n/* ��������������� */\r\n#header h1{\r\ntop:2px;\r\nleft:0;\r\ncolor:#555;\r\n}\r\n\r\n/* ������ */\r\n#header h2{\r\nleft:0;\r\n}\r\n\r\n/* ������ */\r\n#header p{\r\nright:0;\r\n}\r\n\r\n\r\n/************************************\r\n/* ������������������������������\r\n************************************/\r\nul#mainNav{\r\nposition:absolute;\r\ntop:102px;\r\nheight:45px;\r\nbackground:url(images/mainNavBg.png) no-repeat 0 0;\r\n}\r\n\r\nul#mainNav li{\r\ntext-indent: -5000px;\r\nfloat:left;\r\n}\r\n\r\nul#mainNav a{\r\ndisplay: block;\r\nwidth: 176px;\r\nheight: 45px;\r\nbackground:url(images/mainNav1.jpg) no-repeat 0 0;\r\n}\r\n\r\nul#mainNav li.current_page_item a,ul#mainNav li.current-menu-item a,ul#mainNav li a:hover{background-position:0 -45px;}\r\n\r\nul#mainNav li.menu-item-2 a{background-image:url(images/mainNav2.jpg);}\r\nul#mainNav li.menu-item-3 a{background-image:url(images/mainNav3.jpg);}\r\nul#mainNav li.menu-item-4 a{background-image:url(images/mainNav4.jpg);}\r\nul#mainNav li.menu-item-5 a{background-image:url(images/mainNav5.jpg);}	\r\n\r\n\r\n\r\n/*******************************\r\n/* ���������\r\n*******************************/\r\n#mainImg{margin-bottom:20px;}\r\n\r\nh3.heading{\r\nclear:both;\r\npadding-left:30px;\r\nline-height:34px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\nbackground:url(images/headingBg.png) no-repeat 0 0;\r\n}\r\n\r\n.article{\r\nborder:0;\r\nmargin:0 0 20px 0;\r\npadding: 0 10px 0 10px;\r\nbackground:none;\r\nborder:1px solid #dcdcdc;\r\n}\r\n\r\n.article_cell{\r\nclear:both;\r\npadding:20px 0 25px;\r\nborder-bottom:1px dashed #dcdcdc;\r\n}\r\n\r\n.main{\r\npadding:20px 10px 20px 10px;\r\nmargin:0 0 20px 0;\r\nborder:0;\r\nbackground:none;\r\nborder:1px solid #dcdcdc;\r\n}\r\n\r\n.last{border-bottom:none;}\r\n\r\n.main h4{\r\nmargin:0 0 10px 10px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\n}\r\n\r\n.article h4{\r\nmargin:0 0 10px 10px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\n}\r\n\r\n.alignleft{\r\nfloat:left;\r\npadding:0 15px 15px 10px;\r\n}\r\n\r\n.alignright{\r\nfloat:right;\r\npadding: 0 10px 15px 15px;\r\n}\r\n\r\n.aligncenter{\r\npadding: 20px 0 20px 0;\r\ntext-align: center;\r\n}\r\n\r\n.main p{\r\npadding:0 10px 0 10px;\r\n}\r\n\r\n.article p{\r\npadding:0 10px 0 10px;\r\n}\r\n\r\n.picture{\r\nwidth:193px;\r\nfloat:left;\r\ntext-align:center;\r\nbackground-color:#ffffff;\r\nborder-top:1px solid #eaeaea;\r\nborder-right:1px solid #ddd;\r\nborder-bottom:1px solid #ccc;\r\nborder-left:1px solid #eaeaea;\r\npadding:5px 0 5px 0;\r\nmargin:5px 0 0 5px;\r\n}\r\n\r\n.picture:hover{\r\nbackground-color:#fffaef;\r\n}\r\n\r\n\r\n/*******************************\r\n/* ���������������\r\n*******************************/\r\n#sidebar h3{\r\nclear:both;\r\npadding-left:30px;\r\nline-height:34px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\nbackground:url(images/side_headingBg.png) no-repeat 0 0;\r\n}\r\n\r\nul.info{\r\noverflow:hidden;\r\npadding:0 0 10px 17px;\r\nborder:1px solid #dcdcdc;\r\nmargin-bottom:20px;\r\n}\r\n\r\nul.info li{\r\nline-height:0;\r\npadding:10px 0;\r\nmargin-right:15px;\r\nborder-bottom:1px dashed #dcdcdc;\r\n}\r\n\r\nul.info a:link,ul.info a:visited{\r\ndisplay: block;\r\npadding-left:12px;\r\nline-height:normal;\r\ntext-decoration:none;\r\ncolor:#313131;\r\nbackground:url(images/linkArrow.gif) no-repeat 0 50%;\r\n}\r\n\r\nul.info a:hover, ul.info li.current_page_item a, ul.info li.current-menu-item a{color:#438918;}\r\n\r\nul.info li.last{border-bottom:none;}\r\n\r\n#sidebar p{margin-bottom:20px;}\r\n\r\n/*******************************\r\n/* ������������\r\n*******************************/\r\n#footer{\r\nclear:both;\r\nbackground:#a2ae52;\r\n}\r\n\r\n#footer ul{\r\npadding:25px 0;\r\ntext-align:center;\r\n}\r\n\r\n#footer li{\r\ndisplay: inline;\r\npadding: 5px 16px;\r\nborder-left:1px dotted #e2f0d9;\r\n}\r\n\r\n#footer li a{\r\ntext-decoration:none;\r\ncolor:#fff;\r\n}\r\n\r\n#footer li a:hover{color:#e2f0d9;}\r\n\r\np#copy{\r\npadding:10px 0 37px;\r\ntext-align:center;\r\ncolor:#fff;\r\nfont-size:10px;\r\n}"
					+ "";

			Mockito.when(reqhelper.isJavaScript("text/javascript")).thenReturn(false);
			Mockito.when(reqhelper.isCss("text/css")).thenReturn(true);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");

		} catch (Exception e) {
			e.printStackTrace();
		}

		result = MinificationImpl.runTest(tracedata);

		assertEquals(
				"Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.",
				result.getAboutText());
		assertEquals("Minify CSS, JS and HTML", result.getDetailTitle());
		assertEquals("File Download: Minify CSS, JS and HTML", result.getOverviewTitle());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());

	}

	/**
	 * tests Javascript compression
	 */
	@Test
	public void runTest_4() {
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);

		Mockito.when((TraceDirectoryResult) tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);

		MinificationImpl = (MinificationImpl) context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentType()).thenReturn("text/javascript");
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = "" + "/*! jQuery v1.7.1 jquery.com | jquery.org/license */" + ""
					+ "(function(a, b) {\r\n	function myFunction(p1, p2) {\r\n	    return p1 * p2;\r\n	}\r\n})(window);"
					+ "";

			Mockito.when(reqhelper.isJavaScript("text/javascript")).thenReturn(true);
			Mockito.when(reqhelper.isCss("text/css")).thenReturn(false);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");

		} catch (Exception e) {
			e.printStackTrace();
		}

		result = MinificationImpl.runTest(tracedata);

		assertEquals(
				"Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.",
				result.getAboutText());
		assertEquals("Minify CSS, JS and HTML", result.getDetailTitle());
		assertEquals("File Download: Minify CSS, JS and HTML", result.getOverviewTitle());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());

	}

	/**
	 * tests JSON compression
	 */
	@Test
	public void runTest_5() {
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);

		Mockito.when((TraceDirectoryResult) tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);

		MinificationImpl = (MinificationImpl) context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentType()).thenReturn("application/json");
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = "[\n    {\n        \"name\": \"Dow Jones Industrial Average\",\n        \"display_name\": \"Dow Jones Industrial Average\",\n        \"symbol\": \"^DJI\",\n        \"price\": \"15,291.36\",\n        \"change\": \"-96.22\",\n        \"per_change\": \"-0.63%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^DJI/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    },\n    {\n        \"name\": \"S&P 500\",\n        \"display_name\": \"S&P 500\",\n        \"symbol\": \"^GSPC\",\n        \"price\": \"1,651.56\",\n        \"change\": \"-17.60\",\n        \"per_change\": \"-1.05%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^GSPC/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    },\n    {\n        \"name\": \"NASDAQ Composite\",\n        \"display_name\": \"NASDAQ Composite\",\n        \"symbol\": \"^IXIC\",\n        \"price\": \"3,451.57\",\n        \"change\": \"-50.55\",\n        \"per_change\": \"-1.44%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^IXIC/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    },\n    {\n        \"name\": \"FTSE 100\",\n        \"display_name\": \"FTSE 100\",\n        \"symbol\": \"^FTSE\",\n        \"price\": \"6,840.27\",\n        \"change\": \"36.40\",\n        \"per_change\": \"+0.53%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^FTSE/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    }\n]";

			Mockito.when(reqhelper.isJSON("application/json")).thenReturn(true);
			Mockito.when(reqhelper.isCss("text/css")).thenReturn(false);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");

		} catch (Exception e) {
			e.printStackTrace();
		}

		result = MinificationImpl.runTest(tracedata);

		assertEquals(
				"Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.",
				result.getAboutText());
		assertEquals("Minify CSS, JS and HTML", result.getDetailTitle());
		assertEquals("File Download: Minify CSS, JS and HTML", result.getOverviewTitle());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("PASS", result.getResultType().toString());
	}
}
