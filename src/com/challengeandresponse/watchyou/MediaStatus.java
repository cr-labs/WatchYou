package com.challengeandresponse.watchyou;

/**
 * Status record describing the retrieval status of a MediaRecord record from a service
 * what were the HTTP and service responses to the request?
 * 
 * @author jim
 */
public class MediaStatus {

	/**
	 * msec timestamp when this status record was created
	 */
	public long		statusTimeMsec;
	/**
	 * Summary error/ok status considering service and HTTP responses
	 */
	public int 		errorCode;
	/**
	 * Summary error/ok message considering service and HTTP responses
	 */
	public String 	errorMessage;

	/**
	 * The HTTP response code from attempting to query the service via http
	 */
	public int		serviceHttpResponseCode;
	/**
	 * The actual response to the query, from the service, could be a character string or a value, depending on the service
	 */
	public String	serviceResponse;
	/**
	 * The HTTP response code from attempting to query a web page about the media object
	 */
	public int		webpageHttpResponseCode;

	/**
	 * The full text response from a service inquiry associated with this MediaStatus object
	 */
	public String fullServiceResponse;

	
	/**
	 * The error code for "OK", that is, no error
	 */
	public static final int		ERRORCODE_OK = 0;
	
	/**
	 * The error code to indicate no error code has been set
	 */
	public static final int		ERRORCODE_NONE = -1;
	/**
	 * The error code for an unknown error when contacting the service
	 */
	public static final int		ERRORCODE_UNKNOWN = 10000;
	/**
	 * The media ID was not valid
	 */
	public static final int		ERRORCODE_INVALID_ID = 10001;
	/**
	 * The response from the service indicated an error but the response could not be read
	 */
	public static final int		ERRORCODE_UNREADABLE = 10002;
	/**
	 * The media was removed for TOS violations
	 */
	public static final int		ERRORCODE_TAKEDOWN = 10003;
	/**
	 * A media object having the given Media ID was not found
	 */
	public static final int		ERRORCODE_MEDIAID_NOT_FOUND = 10004;
	



	
	public MediaStatus() {
		statusTimeMsec = System.currentTimeMillis();
		errorCode =	ERRORCODE_NONE;
		errorMessage = "";
		serviceHttpResponseCode = 0;
		serviceResponse = "";
		webpageHttpResponseCode = 0;
		fullServiceResponse = "";
	}

	
}