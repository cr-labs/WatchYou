<%@ page import="com.challengeandresponse.watchyou.*" %>

<%@ include file="header.jsp" %>
<% sel.addEvent(request,"sendAuthenticationAgain.jsp"); %>

<div align="center">
<%@ include file="showAndClearMessage.jsp" %>

<div class="titleInverted" align="center">Re-send my E-mail validation</div><br />

<p><b>Please enter your WatchYou username and click Send to have the authentication code sent to your e-mail address again.</b>
</p>

<p>
<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method='post'>
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_RESEND_VALIDATION%>' />
<table width=300>
<tr><td align="right">User name:</td><td><input type='text' name='<%=WatchYouCredential.FIELD_USERNAME%>'" /></td></tr>
<tr><td>&nbsp;</td><td align="left"><input type='submit' value='Send' /></td></tr>
</table>
</form>
</p>
</div>


<%@ include file="footer.jsp" %>
