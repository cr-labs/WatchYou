<%@ page import="com.challengeandresponse.watchyou.*" %>
<%@ page import="java.util.*" %>

<% 
	//  PAGE IS NOT AVAILABLE UNLESS USER IS LOGGED IN
	if (! LoginLogout.isLoggedIn(session)) {
		response.sendRedirect(WatchYou.HOME_PAGE);
		return;
	}
%>

<%@ include file="header.jsp" %>
<% sel.addEvent(request,"serverStatus.jsp"); %>

<div align="center">
<%@ include file="lookupVideoForm.jsp" %>
&nbsp;<br />
<%@ include file="showAndClearMessage.jsp" %>
<%@ include file="loginStatus.jsp" %>
</div>

<div class="titleInverted" align="center">Server status</div><br />


<p><b>RECENT LOG ENTRIES</b><br />
<%
	// run the query: last (sec) seconds of records, in reverse order newest to oldest
	List<Event> result = sel.getEvents(wyc.serverSecToShow);
	// format the results with <br /> after each record
	out.println("<strong>Last "+wyc.serverSecToShow+" seconds of events</strong><br />");
	Iterator<Event> i = result.iterator();
	while (i.hasNext()) {
				out.println(i.next().toString(wyc.dateFormatSec)+"<br />");
	}
%>
</p>

<p>
<b>SERVER STATUS</b><br />
Uptime: <%=WatchYouUtils.hoursAndMinutesFromMilliseconds(WatchYou.getUptimeMillis(),true)%><br />
Server time: <%=sdf.format(new Date()) %><br />
</p>


<p>
<b>CONFIGURATION</b><br />


Sleep between records: <%=wyc.betweenRecordSleepMsec%> msec<br />
Sleep between rounds: <%=wyc.betweenRoundsSleepMsec%> msec<br />
&nbsp;<br />
Stop querying an item after <%=wyc.maxConsecStopSignals%> consecutive stop signals<br />
Timeouts: Connect <%=wyc.connectTimeoutMsec%> msec; Read timeout <%=wyc.readTimeoutMsec%> msec<br />
Pings to show: <%=wyc.showPingCount%> pings<br />

Media db file: <%=wyc.mediaDBFile%><br />
Event db file: <%=wyc.eventDBFile%><br />
User db file: <%=wyc.userDBFile%><br />
db message level: <%=wyc.dbMessageLevel%><br />
&nbsp;<br />
Show last <%=wyc.serverSecToShow%> seconds of server events<br />
Session max inactive interval: <%=WatchYouUtils.hoursAndMinutesFromSeconds(wyc.sessionMaxInactiveIntervalSec,true)%> <br />
&nbsp;<br />
dateFormat: <%=wyc.dateFormat%><br />
dateFormatSec: <%=wyc.dateFormatSec%><br />
&nbsp;<br />
video width: <%=wyc.videoWidth%><br />
video height: <%=wyc.videoHeight%><br />
&nbsp;<br />
REST endpoint: <%=wyc.restEndpoint%><br />
User Agent: <%=wyc.userAgent%><br />
Video base url: <%=wyc.youTubeVideoBaseUrl%><br />
&nbsp;<br />
JSP absolute path: <%=wyc.jspAbsolutePath%><br />
Servlet absolute path: <%=wyc.servletAbsolutePath%><br />
Authentication page url: <%=wyc.authenticationPageUrl%><br />
&nbsp;<br />
SMTP server: <%=wyc.smtpServer%><br />
E-mail "from" account: <%=wyc.emailFromAccount%><br />
E-mail "from" name: <%=wyc.emailFromName%><br />
Check that e-mail domain exists: <%=wyc.checkEmailDomain%><br />
Minimum delay between emails: <%=WatchYouUtils.hoursAndMinutesFromSeconds(wyc.minDelayBetweenEmailsSec,true)%><br />

&nbsp;<br />
Style sheet: <%=wyc.styleSheet%><br />
YouTube Developer id: not shown
</p>

<%@ include file="footer.jsp" %>
