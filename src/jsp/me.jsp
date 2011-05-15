<%@ include file="header.jsp" %>
<% sel.addEvent(request,"me.jsp"); %>

<div align='center'>
<b>All About ME</b><br />
</div>

<div align="center">
<%@ include file="showAndClearMessage.jsp" %>

<%
WatchYouCredential c = WatchYou.getLoggedInUserRecord(request);
if (c == null) {
	response.sendRedirect(WatchYou.HOME_PAGE);
	return;
}
%>



<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method='post'>
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_UPDATE_USER%>' />
<table border="0">
<tr>
<td align="right">User name:</td>
<td align="left"><b><%=c.username%></b></td>
</tr>
<tr>
<td align="right">E-mail address:</td>
<td align="left"><input type='text' name='<%=WatchYouCredential.FIELD_EMAIL%>' value='<%=c.email%>' />
</td></tr>
<tr>
<td align="right">YouTube username:</td>
<td align="left"><input type='text' name='<%=WatchYouCredential.FIELD_YOUTUBEUSERNAME%>'  value='<%=c.youTubeUserId%>' /> (optional)</td>
</tr>
<tr><td>&nbsp;</td><td align="left"><input type='submit' value='Update my info' /></td></tr>
</table>
</form>

<hr />
<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method='post'>
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_CHANGE_PASSWORD%>' />
<table border="0">
<tr>
<td align="right">New password:</td>
<td align="left"><input type='password' name='<%=WatchYouCredential.FIELD_PASSWORD%>' /></td>
</tr>
<tr>
<td align="right">Type new password again:</td>
<td align="left"><input type='password' name='<%=WatchYouCredential.FIELD_PASSWORD2%>' />
</td></tr>
<tr><td>&nbsp;</td><td align="left"><input type='submit' value='Change my password' /></td></tr>
</table>
</form>



</div>
<%=c.getPrivilegeTokens()%><br />

<%@ include file="footer.jsp" %>
