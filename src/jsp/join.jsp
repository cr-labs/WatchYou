<%@ include file="header.jsp" %>
<% sel.addEvent(request,"join.jsp"); %>

<div align='center'>
<b>Join WatchYou!</b><br />
</div>

<div align="center">
<%@ include file="showAndClearMessage.jsp" %>

<%
// if there was already an attempt to create an account but it failed, 
// then the fields can be re-filled for the user...
WatchYouCredential c = (WatchYouCredential) session.getAttribute(WatchYou.SESSION_CREDENTIAL);
if (c == null)
	c = new WatchYouCredential("");
session.removeAttribute(WatchYou.SESSION_CREDENTIAL);
%>



<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method='post'>
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_ADD_USER%>' />
<table border="0">

<tr>
<td align="right">E-mail address:</td>
<td align="left"><input type='text' name='<%=WatchYouCredential.FIELD_EMAIL%>' value='<%=c.email%>' />
</td></tr>
<tr>
<td align="right">User name:</td>
<td align="left"><input type='text' name='<%=WatchYouCredential.FIELD_USERNAME%>' value='<%=c.username%>' /></td>
</tr>
<tr>
<td align="right">Password:</td>
<td align="left"><input type='password' name='<%=WatchYouCredential.FIELD_PASSWORD%>' value=''/></td>
</tr>
<tr>
<td align="right">Retype password:</td>
<td align="left"><input type='password' name='<%=WatchYouCredential.FIELD_PASSWORD2%>' value=''/></td>
</tr>
<tr>
<td align="right">YouTube username:</td>
<td align="left"><input type='text' name='<%=WatchYouCredential.FIELD_YOUTUBEUSERNAME%>'  value='<%=c.youTubeUserId%>' /> (optional)</td>
</tr>

<tr><td>&nbsp;</td><td align="left"><input type='submit' value='Join!' /></td></tr>
</table>
</form>
</div>


<%@ include file="footer.jsp" %>
