package com.challengeandresponse.watchyou;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.*;
import org.jdom.input.SAXBuilder;


/**
 * REST interface to YouTube
 * 
 * Rest EXAMPLE:<br />
 * http://www.youtube.com/api2_rest?method=youtube.users.get_profile&dev_id=YOUR_DEV_ID&user=YOUTUBE_USER_NAME<br />
 * 
 * @author jim
 */

public class YouTubeREST {
	
	private String	restEndpoint;
	private WatchYouConfig wyc;

	public static final String 	METHOD_GET_DETAILS = "youtube.videos.get_details";

	 // Patterns to match on a video's web page that reveals a takedown
	private static final Pattern	WEBPAGE_REMOVED_TOS = Pattern.compile("This video has been removed due to terms of use violation");
	private static final Pattern	WEBPAGE_REMOVED_AT_REQUEST = Pattern.compile("This video has been removed at the request of copyright owner.*?>");

	private static final Vector <Pattern>	TAKEDOWN_PATTERNS;
	
	
	static {
		 TAKEDOWN_PATTERNS = new Vector <Pattern> ();
		 TAKEDOWN_PATTERNS.add(WEBPAGE_REMOVED_TOS);
		 TAKEDOWN_PATTERNS.add(WEBPAGE_REMOVED_AT_REQUEST);
	}

	// service response messages
	private static final String 	SERVICE_RESPONSE_OK = "ok";
//	private static final String 	SERVICE_RESPONSE_ERROR = "fail";

	
	/**
	 * Initialize this YouTubeRest object, and stash its endpoint
	 * @param restEndpoint The URL of the REST Endpoint... must be a valid URL
	 * @throws MalformedURLException if the restEndpoint is not valid
	 */	
	public YouTubeREST(String restEndpoint, WatchYouConfig wyc)
	throws MalformedURLException {
		new URL(restEndpoint);
		this.restEndpoint = restEndpoint;
		this.wyc = wyc;
	}


	/**
	 * Retrieve latest information for a given YouTubeRecord.<br />
	 * @param ytr the videoID of the YouTubeRecord to update.
	 * @param keepRawResponse set TRUE to keep the full response from the service in the record, false to discard it (can be LARGE and maybe you don't want it in the database)
	 * @return a YouTubeRecord with all the info filled in, having one MediaStatus object in its statusReports field
	 * 
	 */
	public YouTubeRecord fetchVideoById(String videoID, boolean keepRawResponse)
	throws WatchYouException {
		if (videoID == null) {
			throw new WatchYouException("YouTubeREST.fetch() requires a videoID");
		}

		// the new YouTubeRecord
		YouTubeRecord ytr = new YouTubeRecord(videoID);
		// the new MediaStatus object
		MediaStatus ms = new MediaStatus();
		// a URLConnection to be reused below, and that might be referenced in some error responses
		HttpURLConnection urlc = null;
		
		try {
			// 1. retrieve the data from the web service, and set basic status result to best of our ability reading the web service response
			URL req = new URL(restEndpoint+"?method="+METHOD_GET_DETAILS+"&dev_id="+wyc.youTubeDeveloperID+"&video_id="+ytr.mediaID);
			urlc = WatchYouUtils.openURLConnection(req,wyc.userAgent,wyc.connectTimeoutMsec,wyc.readTimeoutMsec);
			ms.serviceHttpResponseCode = urlc.getResponseCode();
			if (ms.serviceHttpResponseCode == HttpURLConnection.HTTP_OK) {
				StringBuffer all = fetchEntireURL(urlc);
				ms = marshallStatus(all.toString()); // interpret the embedded response codes
				if (ms.errorCode == MediaStatus.ERRORCODE_OK)
					ytr.marshallContent(ms.fullServiceResponse);
			}
			else {
				ms.errorCode = MediaStatus.ERRORCODE_UNKNOWN;
				ms.errorMessage="Error making REST query";
			}

			// 2. retrieve and cache thumbnail image if there is one
			if (ytr.thumbnailURL != null) {
				urlc = WatchYouUtils.openURLConnection(new URL(ytr.thumbnailURL),wyc.userAgent,wyc.connectTimeoutMsec,wyc.readTimeoutMsec);
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_OK) {
					ytr.thumbnailContentType = urlc.getContentType();
					DataInputStream dis = new DataInputStream(urlc.getInputStream());
					ytr.cachedThumbnail = new byte[urlc.getContentLength()];
					dis.readFully(ytr.cachedThumbnail);
				}
				urlc.disconnect();
				urlc = null;
			}

			// 3. retrieve the video's web page and parse for a more specific status code
			urlc = WatchYouUtils.openURLConnection(new URL(wyc.youTubeVideoBaseUrl+ytr.mediaID),wyc.userAgent,wyc.connectTimeoutMsec,wyc.readTimeoutMsec);
			ms.webpageHttpResponseCode = urlc.getResponseCode();
			// retrieve the page, then parse for better status
			if (ms.webpageHttpResponseCode == HttpURLConnection.HTTP_OK) {
				StringBuffer all = fetchEntireURL(urlc);
				String td;
				if ((td = findTakedown(all.toString())) != null) {
					ms.errorCode = MediaStatus.ERRORCODE_TAKEDOWN;
					ms.errorMessage = td;
				}
			}
			// if the http response was not HTTP_OK, but the error code is still "OK", correct that
			else if (ms.errorCode == MediaStatus.ERRORCODE_OK){
				ms.errorCode = MediaStatus.ERRORCODE_UNKNOWN;
				ms.errorMessage="Error retrieving web page";
			}
		}
		catch (MalformedURLException mue) {
			ms.errorCode = MediaStatus.ERRORCODE_UNKNOWN;
			ms.errorMessage = "Malformed URL when getting status"+((urlc != null) ? ":"+urlc.getURL().toString() : "");
		}
		catch (SocketTimeoutException ste) {
			ms.errorCode = MediaStatus.ERRORCODE_UNKNOWN;
			ms.errorMessage="Socket timeout when getting status:"+ste.getMessage();
		}
		catch (IOException ioe) {
			ms.errorCode = MediaStatus.ERRORCODE_UNKNOWN;
			ms.errorMessage = "IO Exception when getting status"+ioe.getMessage();
		}

		// append the new status report to this YouTubeRecord
		// CLEAR the fullServiceResponse from the MediaStatus record, if we're not saving that (to save space)
		if (! keepRawResponse)
			ms.fullServiceResponse = "";
		ytr.statusReports.add(ms);
		return ytr;
	}



	
	/**
	 * Convenience method to just fetch an entire URL into a StringBuffer
	 * @param urlc
	 * @return
	 * @throws IOException
	 */
	private StringBuffer fetchEntireURL(HttpURLConnection urlc)
	throws IOException {
		StringBuffer all = new StringBuffer();
		try {	
			BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String inputLine;
			all = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
				all.append(inputLine);
			in.close();
		}
		finally {
			urlc.disconnect();
			urlc = null;
		}
		return all;
	}

	

	

	/**
	 * Interpret YouTube XML header stuff into a MediaStatus object
	 * @return a MediaStatus object with as much status info as was findable from the xmlString
	 */

	private MediaStatus marshallStatus(String xmlString)
	throws WatchYouException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		MediaStatus ms = new MediaStatus();
		ms.fullServiceResponse = xmlString;

		try {
			doc = builder.build(new StringReader(xmlString));
		}
		catch (JDOMException jdome) {
			throw new WatchYouException("Exception interpreting xmlString: "+jdome.getMessage());
		}
		catch (IOException ioe) {
			throw new WatchYouException("Exception interpreting xmlString: "+ioe.getMessage());
		}

		Element rootEl = doc.getRootElement(); // the root element is <ut_response status=____>
		ms.serviceResponse = rootEl.getAttributeValue("status");
		ms.errorCode = (YouTubeREST.SERVICE_RESPONSE_OK.equals(ms.serviceResponse) ? MediaStatus.ERRORCODE_OK : MediaStatus.ERRORCODE_UNKNOWN);

		if (ms.errorCode != MediaStatus.ERRORCODE_OK) {
			Element errorEl = rootEl.getChild("error");
			ms.errorCode = MediaRecord.parseInt(errorEl.getChildTextTrim("code"),MediaStatus.ERRORCODE_UNREADABLE);
			ms.errorMessage = errorEl.getChildTextTrim("description");
		} // end of marshalling the error response

		// return whatever was found
		return ms;
	}

	
	/**
	 * If a takedown string exists in the message, return it
	 * @param message
	 * @return the message fragment indicating the issue, or null if there was no match
	 */
	private String findTakedown(String message) {
		Enumeration <Pattern> e = TAKEDOWN_PATTERNS.elements();
		while (e.hasMoreElements()) {
			Matcher m = e.nextElement().matcher(message);
			if (m.find())
				return m.group();
		}
		return null;
	}

	
}
