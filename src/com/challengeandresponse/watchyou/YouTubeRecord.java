package com.challengeandresponse.watchyou;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * One YouTubeRecord -- base info returned by any inquiry
 * 
 * <code>
 * <video_details>
 *     <author>youtubeuser</author>
 *     <title>My Trip to California</title>
 *     <rating_avg>3.25</rating_avg>
 *     <rating_count>10</rating_count>
 *     <tags>california trip redwoods</tags>
 *     <description>This video shows some highlights of my trip to California last year.</description>
 *     <update_time>1129803584</update_time> <!-- UNIX time, secs since 1/1/70 -->
 *     <view_count>7</view_count>
 *     <upload_time>1127760809</upload_time> <!-- UNIX time, secs since 1/1/70 -->
 *     <length_seconds>8</length_seconds>
 *    <recording_date>None</recording_date>
 *     <recording_location/>
 *     <recording_country/>
 *     <comment_list>
 *         <comment>
 *             <author>steve</author>
 *             <text>asdfasdf</text>
 *             <time>1129773022</time>
 *         </comment>
 *     </comment_list>
 *     <channel_list>
 *         <channel>Humor</channel>
 *         <channel>Odd & Outrageous</channel>
 *    </channel_list>
 *         <thumbnail_url>http://static205.youtube.com/vi/bkZHmZmZUJk/2.jpg</thumbnail_url>
 * </video_details>
 * 
 * </code>
 */

public class YouTubeRecord extends MediaRecord {

	public String	recordingDate;
	public String	recordingLocation;
	public String	recordingCountry;
	public Vector<String>	channelList;


	/**
	 * The length of the MediaID field on the YouTubeRecord input form
	 */
	public static final int		MEDIAID_LENGTH = 11;
	
	
	
	/**
	 * This constructor initializes all field values to defaults, and sets videoID to videoID. All Strings are set
	 * to null; numeric values are set to 0.
	 * @param videoID the videoID this is about, or null to create an empty, uninitialized object for QBE usage
	 */
	public YouTubeRecord(String videoID) {
		super(videoID);
		if (videoID != null) {
			this.recordingDate = null;
			this.recordingLocation = null;
			this.recordingCountry = null;
			this.channelList = new Vector<String>();
		}
	}



	/**
	 * Stuff YouTube XML response into a YouTubeRecord, no status -- main fields only
	 * @param xmlString
	 */
	public void marshallContent(String xmlString)
	throws WatchYouException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(new StringReader(xmlString));
		}
		catch (JDOMException jdome) {
			throw new WatchYouException("JDOMException in YouTubeRecord.marshallContent:"+jdome.getMessage());
		}
		catch (IOException ioe) {
			throw new WatchYouException("IOException in YouTubeRecord.marshallContent:"+ioe.getMessage());
		}

		Element rootEl = doc.getRootElement(); // the root element is <ut_response status=____>
		Element videoEl = rootEl.getChild("video_details");
		author = videoEl.getChildTextTrim("author");
		title = videoEl.getChildTextTrim("title");
		tags = videoEl.getChildTextTrim("tags");
		description = videoEl.getChildTextTrim("description");
		recordingDate = videoEl.getChildTextTrim("recording_date");
		recordingLocation = videoEl.getChildTextTrim("recording_location");
		recordingCountry = videoEl.getChildTextTrim("recording_country");
		this.ratingAvg = parseDouble(videoEl.getChildTextTrim("rating_avg"),0.0d);
		this.ratingCount = parseInt(videoEl.getChildTextTrim("rating_count"),0);
		this.updateTime = parseLong(videoEl.getChildTextTrim("update_time"),0l);
		this.viewCount = parseInt(videoEl.getChildTextTrim("view_count"),0);
		this.uploadTime = parseLong(videoEl.getChildTextTrim("upload_time"),0l);
		this.lengthSeconds = parseInt(videoEl.getChildTextTrim("length_seconds"),0);
		// pick off the image thumbnail URL, but pre-validate it even though it's stored as a string
		this.thumbnailURL = parseURLToString(videoEl.getChildTextTrim("thumbnail_url"),null);
		// fetch the channel list if any
		this.channelList = new Vector<String>();
		Element channelEl = videoEl.getChild("channel_list");
		if (channelEl != null) {
			Iterator it = channelEl.getChildren().iterator();
			while (it.hasNext()) {
				Element el = (Element) it.next();
				this.channelList.add(el.getText());
			}
		} 
	}

	/**
	 * @param queryItem
	 * @return true if this queryItem looks OK for running a query (basically is it the correct length?), false otherwise
	 */
	public static boolean validQueryItem(String queryItem) {
		if (queryItem == null)
			return false;
		return true;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("recordingDate: "+recordingDate+"\n");
		sb.append("recordingLocation: "+recordingLocation+"\n");
		sb.append("recordingCountry: "+recordingCountry+"\n");
		sb.append("channelList size: "+channelList.size()+"\n");
		return sb.toString();
	}



}
