package com.challengeandresponse.watchyou;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * One MediaRecord -- root class of all media records.
 * This class should be extended for every specific kind of service.
 * 
 */

public class MediaRecord {
	
	public String 	mediaID;
	public String	author;
	public String	title;
	public double	ratingAvg;
	public int		ratingCount;
	public String	tags;
	public String	description;
	public long		updateTime;
	public int		viewCount;
	public long		uploadTime;
	public int		lengthSeconds;
	public String	thumbnailURL;
	/**
	 * The full text of the response from a service query
	 */
	public String 	fullServiceResponse;

	/**
	 * A collection of all status reports collected on this object. 
	 * First entry in the list is the initial status of the object. to access: statusReports.get(0)
	 * Last entry in the list is the most recent status of the object. to access: statusReports.get(statusReports.size() - 1)
	 */
	public ArrayList<MediaStatus> statusReports;
	/**
	 * The bytes of the cached thumbnail image, exactly as received from the server
	 */
	public byte[] 	cachedThumbnail;
	/**
	 * The contentType reported by the server, for the thumbnail image (we'll send this back when sending the cached thumbnail)
	 */
	public String	thumbnailContentType;
	/**
	 * True if this item is tracked (the record came from the database), false otherwise (the record came from a REST query)
	 */
	public boolean	tracked;
	/**
	 * Which WatchYou user added this record?
	 */
	public String 	addedByUser;

	
	protected int 		PARSE_FAIL_INT 		= Integer.MIN_VALUE;
	protected double 	PARSE_FAIL_DOUBLE 	= Double.MIN_VALUE;
	protected long 		PARSE_FAIL_LONG 	= Long.MIN_VALUE;
	
	
		
	/**
	 * This constructor sets NO default values in the object... all fields are left uninitialized.
	 * This is useful for QBE queries in db4o, but otherwise, the constructor with 'videoID' should be used.
	 */
	private MediaRecord() {
		
	}
		
	/**
	 * This constructor initializes all field values to defaults, and sets videoID to videoID. All Strings are set
	 * to null; numeric values are set to 0.
	 * @param mediaID the MediaID to load into the object, this object is "about" this mediaID item; or NULL to get a completely empty object for QBE use
	 */
	public MediaRecord(String mediaID) {
		if (mediaID != null) {
			this.mediaID = mediaID;
			this.author = null;
			this.title = null;
			this.ratingAvg = 0.0d;
			this.tags = null;
			this.description = null;
			this.updateTime = 0l;
			this.viewCount = 0;
			this.uploadTime = 0l;
			this.lengthSeconds = 0;
			this.thumbnailURL = null;
			this.statusReports = new ArrayList<MediaStatus>() ;
			this.cachedThumbnail = null;
			this.thumbnailContentType = null;
			this.tracked = false;
			this.addedByUser = null;
		}
	}
	
	
	
	/**
	 * @return  the first Status record, or null if there are no status records
	 */
	public MediaStatus getFirstStatus() {
		if (statusReports.size() > 0)
			return statusReports.get(0);
		else
			return null;
	}
	
	/**
	 * @return  the last Status record, or null if there are no status records
	 */
	public MediaStatus getLatestStatus() {
		if (statusReports.size() > 0)
			return statusReports.get(statusReports.size()-1);
		else
			return null;
	}

	
	
	/**
	 * Parse safely an int from a string
	 * @param s
	 * @return the int value of s, or default if parsing failed
	 */
	protected static int parseInt(String s, int dflt) {
		if (s == null)
			return dflt;
		try {
			return Integer.parseInt(s.trim());
		}
		catch (NumberFormatException nfe) {
			return dflt;
		}
	}
	
	/**
	 * Parse safely a double from a string
	 * @param s
	 * @return the double value of s, or PARSE_FAIL_DOUBLE if parsing failed
	 */
	protected static double parseDouble(String s, double dflt) {
		if (s == null)
			return dflt;
		try {
			return Double.parseDouble(s.trim());
		}
		catch (NumberFormatException nfe) {
			return dflt;
		}
	}
	
	/**
	 * Parse safely a long from a string
	 * @param s
	 * @return the long value of s, or PARSE_FAIL_LONG if parsing failed
	 */
	protected static long parseLong(String s, long dflt) {
		if (s == null)
			return dflt;
		try {
			return Long.parseLong(s.trim());
		}
		catch (NumberFormatException nfe) {
			return dflt;
		}
	}

	/**
	 * Receive a prospective URL as a string, check it, if it's a valid URL, the String is returned, if not, the dflt value is returned
	 * @param s The string to check to see if it's a valid URL
	 * @param dflt The value to return if s is not a valid URL
	 * @return s if s is a valid URL, dflt if not
	 */
	protected static String parseURLToString(String s, String dflt) {
		if ((s != null) && (s.length() > 10)) {
			 try {
				 new URL(s);
			 }
			 catch (MalformedURLException mue) {
				 return dflt;
			 }
			 return s;
		}
		else
			return dflt;
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		MediaStatus status;
		sb.append("video ID: "+mediaID+"\n");
		sb.append("author: "+author+"\n");
		sb.append("title: "+title+"\n");
		sb.append("ratingAvg: "+ratingAvg+"\n");
		sb.append("tags: "+tags+"\n");
		sb.append("description: "+description+"\n");
		sb.append("updateTime: "+updateTime+"\n");
		sb.append("viewCount: "+viewCount+"\n");
		sb.append("uploadTime: "+uploadTime+"\n");
		sb.append("lengthSeconds: "+lengthSeconds+"\n");
		sb.append("thumbnailURL: "+thumbnailURL+"\n");
		sb.append("cached thumbnail length: "+((cachedThumbnail != null) ? cachedThumbnail.length:"null")+"\n");
		sb.append("thumbnail content type: "+thumbnailContentType+"\n");
		sb.append("added by user: "+addedByUser+"\n");

		// report initial status, if there is a record, or state that there are no status records
		if (statusReports.size() > 0) {
			status = getFirstStatus();
			sb.append("initial status timestamp: "+status.statusTimeMsec+"\n");
			sb.append("initial errorCode: "+status.errorCode+"\n");
			sb.append("initial errorMessage: "+status.errorMessage+"\n");
			sb.append("initial serviceHttpResponseCode: "+status.serviceHttpResponseCode+"\n");
			sb.append("initial webpageHttpResponseCode: "+status.webpageHttpResponseCode+"\n");
			sb.append("initial service response: "+status.serviceResponse+"\n");
			sb.append("initial status is OK: "+(status.errorCode == MediaStatus.ERRORCODE_OK)+"\n");
		}
		else {
			sb.append("No status records\n");
		}
		// if there's more than one status record, also report the last one
		if (statusReports.size() > 1) {
			status = getLatestStatus();
			sb.append("latest status timestamp: "+status.statusTimeMsec+"\n");
			sb.append("latest errorCode: "+status.errorCode+"\n");
			sb.append("latest errorMessage: "+status.errorMessage+"\n");
			sb.append("latest serviceHttpResponseCode: "+status.serviceHttpResponseCode+"\n");
			sb.append("latest webpageHttpResponseCode: "+status.webpageHttpResponseCode+"\n");
			sb.append("latest service response: "+status.serviceResponse+"\n");
			sb.append("latest status is OK: "+(status.errorCode == MediaStatus.ERRORCODE_OK)+"\n");
		}
		
		return sb.toString();
	}
	
	
	
}
