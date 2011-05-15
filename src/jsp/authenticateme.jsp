<%@ page import="com.challengeandresponse.watchyou.*" %>

<%@ include file="header.jsp" %>
<% sel.addEvent(request,"authenticateme.jsp"); %>

<div align="center">
<%@ include file="showAndClearMessage.jsp" %>

<%
// if there was a GET of this page with username and authentication code, prefill the form
String username = request.getParameter(WatchYouCredential.FIELD_USERNAME);
if (username == null)
	username = "";
String authcode = request.getParameter(WatchYouCredential.FIELD_AUTHCODE);
if (authcode == null)
	authcode = "";
%>

<div class="titleInverted" align="center">WatchYou E-mail validation</div><br />

<p><b>Please enter your WatchYou username and the authentication code
we sent to your e-mail address below, to activate your account.</b><br />
</p>

<p>
<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method='post'>
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_VALIDATE_USER%>' />
<table width=300>
<tr><td align="right">User name:</td><td><input type='text' name='<%=WatchYouCredential.FIELD_USERNAME%>' value="<%=username%>" /></td></tr>
<tr><td align="right">Authentication code:</td><td><input type='text' name='<%=WatchYouCredential.FIELD_AUTHCODE%>' value="<%=authcode%>"/></td></tr>
<tr><td>&nbsp;</td><td align="left"><input type='submit' value='Authenticate me' /></td></tr>
<tr><td>&nbsp;</td><td>&nbsp;<br /><a href="sendAuthenticationAgain.jsp">Send my authentication code again</a></td></tr>
</table>
</form>
</p>
</div>


<%@ include file="footer.jsp" %>
