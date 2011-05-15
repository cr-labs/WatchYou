package com.challengeandresponse.watchyou;

import java.io.IOException;
import java.net.*;

public class WatchYouUtils {
	
	
	public static HttpURLConnection openURLConnection(URL _url, 
			String userAgent, int connectTimeoutMsec, int readTimeoutMsec)
	throws IOException, SocketTimeoutException {
		HttpURLConnection urlc = (HttpURLConnection) _url.openConnection();
		urlc.setRequestProperty("User-Agent",userAgent);
		urlc.setInstanceFollowRedirects(true);
		urlc.setUseCaches(false);
		urlc.setConnectTimeout(connectTimeoutMsec);
		urlc.setReadTimeout(readTimeoutMsec);
		urlc.connect();
		return urlc;
	}

	
	public static String disableAndTrimTags(String unescapedText) {
		if (unescapedText == null)
			return null;
		else
			return unescapedText.trim().replaceAll(">", "&gt;").replaceAll("<", "&lt;");
	}

	
	/**
	 * @param seconds	number of seconds to convert to days/hours/minutes/seconds
	 * @param showSeconds set true to include seconds, false to stop at nearest minute
	 * @return
	 */
	public static String hoursAndMinutesFromSeconds(int seconds, boolean showSeconds) {
		return hoursAndMinutesFromMilliseconds((long) (seconds * 1000),showSeconds);
	}


	/**
	 * @param milliseconds	number of seconds to convert to days/hours/minutes/seconds
	 * @param showSeconds set true to include seconds, false to stop at nearest minute
	 * @return
	 */
	public static String hoursAndMinutesFromMilliseconds(long milliseconds, boolean showSeconds) {
		int uptime = (int) (milliseconds/1000);
		int days = (uptime / 60 / 60 / 24);
		uptime -= days * 60 * 60 *24;
		int hours = (uptime / 60 / 60);
		uptime -= hours * 60 * 60;
		int minutes = uptime / 60;
		uptime -= minutes * 60;
		int seconds = uptime;
		
		StringBuffer s = new StringBuffer();
		if (days > 0) {
			s.append(days);
			s.append(" day");
			s.append(days > 1 ? "s ":" ");
		}
		if (hours > 0) {
			s.append(hours);
			s.append(" hour");
			s.append(hours > 1 ? "s ":" ");
		}
		// show minutes if any units follow
		if (minutes > 0) {
			s.append(minutes);
			s.append(" minute");
			s.append(minutes != 1 ? "s":"");
		}
		if (showSeconds) {
			s.append(" "+seconds);
			s.append(" second");
			s.append(seconds != 1 ? "s ":"");
		}
		return s.toString();
	}

	
	/**
	 * Get the encoded "embed" tag for a given YouTube video, and set the video's display height and width too
	 * @param width		width of the embedded video
	 * @param height	height of the embedded video
	 * @param mr A MediaRecord to get the embed tag for (the type of Media Record will dictate how the embed tag is formatted!)
	 * @return the formatted "embed" tag to drop into a web page for the video 'videoID' at the given height and width
	 */
	public static String getEmbedTag(int width, int height, MediaRecord mr) {
		return "<object width=\""+width+"\" height=\""+height+"\"><param name=\"movie\" value=\"http://www.youtube.com/v/"+mr.mediaID+"\"></param><param name=\"wmode\" value=\"transparent\"></param><embed src=\"http://www.youtube.com/v/"+mr.mediaID+"\" type=\"application/x-shockwave-flash\" wmode=\"transparent\" width=\""+width+"\" height=\""+height+"\"></embed></object>";
	}
	

	
	
}