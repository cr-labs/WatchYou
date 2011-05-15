<%@ page import="java.util.*,com.challengeandresponse.watchyou.*" %>

<% //  PAGE IS NOT AVAILABLE UNLESS USER IS LOGGED IN
	if (! LoginLogout.isLoggedIn(session)) {
		response.sendRedirect(WatchYou.HOME_PAGE);
		return;
	}
%>
<%@ include file="header.jsp" %>
<% sel.addEvent(request,"searchVideos.jsp"); %>

<div align="center">
<%@ include file="lookupVideoForm.jsp" %>
&nbsp;<br />
<%@ include file="showAndClearMessage.jsp" %>
<%@ include file="loginStatus.jsp" %>
</div>


<div class="titleInverted" align="center">Tracked media</div><br />
<p>
	<table valign="top" width="100%">
<%
		List result = WatchYou.getMediaRecords(new MediaRecord(null),request);
		Iterator<YouTubeRecord> i = result.iterator();
		while (i.hasNext()) {
			YouTubeRecord ytr = i.next();
			%>
			<tr>
			<td width="15%" valign="top" align="left"><a href="<%=wyc.jspAbsolutePath%>/youTubeVideo.jsp?<%=WatchYou.FIELD_MEDIAID%>=<%=ytr.mediaID%>"><%=ytr.mediaID%></a></td>
			<td width="30%" valign="top" align="left"><%=ytr.title%></td>
			<td width="55%" valign="top" align="left"><%=ytr.description%></td>
			</tr>
			<%
		}
%>
	</table>
</p>

<%@ include file="footer.jsp" %>
