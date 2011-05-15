<%@ page import="java.util.*" %>
<%@ page import="com.challengeandresponse.watchyou.*" %>

<%@ include file="header.jsp" %>
<% sel.addEvent(request,"youTubeVideo.jsp"); %>

<div align="center">
<%@ include file="lookupVideoForm.jsp" %>
&nbsp;<br />
<%@ include file="showAndClearMessage.jsp" %>
<%@ include file="loginStatus.jsp" %>

<%
String mediaID = WatchYouUtils.disableAndTrimTags(request.getParameter(WatchYou.FIELD_MEDIAID));
YouTubeRecord ytr = null;
MediaStatus ms = null; // ms will hold the MediaStatus record that we care about for display here 

///// 1. PROCESSING -- TRY TO RETRIEVE A VIDEO FROM THE DATABASE OR FROM THE SERVICE
// A: The media ID was missing
if ( (mediaID == null) || (! YouTubeRecord.validQueryItem(mediaID)) ) {
	sel.addEvent(request,"mediaID is missing or too short: "+mediaID);
	ms = new MediaStatus();
	ms.errorCode = MediaStatus.ERRORCODE_INVALID_ID;
}
// B: the video IS ALREADY TRACKED, provide the record from the database
else if ((ytr = (YouTubeRecord) WatchYou.getMediaRecord(mediaID,request)) != null) {
	sel.addEvent(request,"mediaID is currently tracked: "+mediaID);
	ytr.tracked = true;
	session.setAttribute(WatchYou.SESSION_YTR, ytr);
	ms = ytr.getLatestStatus();
}
// C: the video IS NOT ALREADY TRACKED
else {
	sel.addEvent(request,"mediaID is NOT currently tracked: "+mediaID);
	try {
		ytr = WatchYou.getYouTubeRecordFromSite(mediaID);
		ytr.tracked = false;
		ms = ytr.getLatestStatus();
	}
	catch (WatchYouException wye) {
		ms = new MediaStatus();
		ms.errorCode = MediaStatus.ERRORCODE_UNKNOWN;
		ms.errorMessage = "An error occurred while retrieving the video: "+wye.getMessage();
	}
	// if a valid video was retrieved from the REST query, then open a session for tracking
	if (ms.errorCode == MediaStatus.ERRORCODE_OK) {
		sel.addEvent(request,"Data retrieved for mediaID: "+ytr.mediaID);
		session.setAttribute(WatchYou.SESSION_YTR, ytr);
	}
	else {
		sel.addEvent(request,"Data not found for mediaID : "+mediaID+" errorcode/message: "+ms.errorCode+":"+ms.errorMessage);
	}
}


///// 2. DISPLAY -- DISPLAY THE RESULTS FROM STEP 1

if (MediaStatus.ERRORCODE_INVALID_ID == ms.errorCode) {
%>
	<div class="titleInverted" align="center">Invalid media ID <div class="bigBold"><%=mediaID%></div></div><br />
	<div align="center">Cannot look up video. The media ID was not valid.</div><br />
	<div align="center"><%=ms.errorCode%> : <%=ms.errorMessage%></div><br />
<%
}
else if (MediaStatus.ERRORCODE_MEDIAID_NOT_FOUND == ms.errorCode) {
%>
	<div class="titleInverted" align="center">mediaID was not found <div class="bigBold"><%=mediaID%></div></div><br />
	<div align="center">No video was found with that mediaID.</div><br />
	<div align="center"><%=ms.errorCode%> : <%=ms.errorMessage%></div><br />
<%
}
// there is a YTR, but it holds an error, so show the error
else if (MediaStatus.ERRORCODE_OK != ms.errorCode) {
%>
	<div class="titleInverted" align="center">Could not retrieve video <div class="bigBold"><%=mediaID%></div></div><br />
	<div align="center"><%=ms.errorCode%> : <%=ms.errorMessage%></div><br />
<%
} 
// otherwise display the goodies about the video
else {
%>
	<div class="titleInverted" align="center"><%=ytr.title%> <div class="bigBold"><%=ytr.mediaID%></div></div><br />
	<table width="100%" border="0">
	<tr>
	<td width="<%=(wyc.videoWidth+10)%>" align="left" valign="top"><%=WatchYouUtils.getEmbedTag(wyc.videoWidth,wyc.videoHeight,ytr)%></td>
	<td align="left" valign="top">
	<b><%=ytr.description%></b><br />
	<%=WatchYouUtils.hoursAndMinutesFromSeconds(ytr.lengthSeconds,true)%><br />
	Author: <%=ytr.author%><br />
	Uploaded: <%=sdf.format(new Date(ytr.uploadTime*1000))%><br />
	Updated: <%=sdf.format(new Date(ytr.updateTime*1000))%><br />
	Stats: <%=ytr.viewCount%> views; average rating: <%=ytr.ratingAvg%> (<%=ytr.ratingCount%> ratings)<br />
	Tags: <%=ytr.tags%><br />
<%
	if ((ytr.recordingDate != null) && (ytr.recordingDate.length() > 0))
		out.println("Recording date: "+ytr.recordingDate+"<br />");
	if ((ytr.recordingLocation != null) && (ytr.recordingLocation.length() > 0))
		out.println("Recording location: "+ytr.recordingLocation+"<br />");
	if ((ytr.recordingCountry != null) && (ytr.recordingCountry.length() > 0))
		out.println("Recording country: "+ytr.recordingCountry+"<br />");
	if (ytr.channelList.size()>0) {
		out.println("Channels: ");
		Iterator i = ytr.channelList.iterator();
		while (i.hasNext())
			out.println((String) i.next()+" ");
		out.println("<br />");
	}

	// NOT TRACKED
	if (! ytr.tracked) {
		out.println("&nbsp;<br />This video is not currently tracked<br />");
		if (LoginLogout.isLoggedIn(session)) {
			%>
			<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method='post' class='tight'>
			<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_ADD_VIDEO%>' />
			<input type='submit' value='Track this video' />
			</form>
			<%
		}
		else {
			out.println("<b><a href='./login.jsp'>Log In</a> or <a href='join.jsp'>join WatchYou</a> to start a tracker for this video!<br /></b>");
		}
	}
	// TRACKED
	else {
		if ((ytr.cachedThumbnail != null) && (ytr.cachedThumbnail.length > 0)) {
			%>
			&nbsp;<br />
			<img src="<%=wyc.servletAbsolutePath%>/WatchYou?action=image&<%=WatchYou.FIELD_MEDIAID%>=<%=ytr.mediaID%>" width="65"><br />&nbsp;<br />
			<%
		}
		else if (ytr.thumbnailURL != null) {
			out.println("&nbsp;<br /><img src=\""+ytr.thumbnailURL+"\" width=\"65\"><br />&nbsp;<br />");
		}
		else {
			out.println("&nbsp;<br /><em>Thumbnail image not available</em><br />");
		}
		// video is tracked so report that...
		out.println("<b>This video is currently tracked</b> "+ytr.statusReports.size()+"<br />");
		out.println("First contact: "+sdf.format(new Date(ytr.getFirstStatus().statusTimeMsec))+"<br />");
		// ... and show the latest status 
		out.println("Latest status: "+sdf.format(new Date(ytr.getLatestStatus().statusTimeMsec))+"<br />");
		out.println("Error code:"+ytr.getLatestStatus().errorCode+" "+ytr.getLatestStatus().errorMessage+"; ");
		out.println("HTTP:"+ytr.getLatestStatus().serviceHttpResponseCode+" (service), "+
				ytr.getLatestStatus().webpageHttpResponseCode+" (web page)<br />");
		out.println("Tracked by: "+((ytr.addedByUser != null) ? ytr.addedByUser:"<em>unknown</em>")+"<br />");
	}
	out.println("</td></tr></table>");
}

%>
</div>

<%@ include file="footer.jsp" %>
