<%@ page import="com.challengeandresponse.watchyou.WatchYou" %>
<%@ page import="com.challengeandresponse.loginlogout.*" %>

<hr />

<center>
<a href="<%=WatchYou.ABOUT_PAGE%>">About</a>
| <a href="<%=WatchYou.HOME_PAGE%>">Home</a>
<%	if (LoginLogout.isLoggedIn(session)) { %>
| <a href="./serverStatus.jsp">Server status</a>
| <a href="./searchVideos.jsp">Search videos</a>
<% } %>
<br />
&nbsp;<br />
<em>Server time: <%= sdf.format(new Date()) %></em><br />
Copyright (c) 2006 Challenge/Response, LLC<br />
</center>
</td></tr></table>
</body></html>
