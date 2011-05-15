<%@ page import="com.challengeandresponse.watchyou.*" %>

<%@ include file="header.jsp" %>
<% sel.addEvent(request,"resetPassword.jsp"); %>

<div align="center">
<%@ include file="showAndClearMessage.jsp" %>

<div class="titleInverted" align="center">Reset my password</div><br />

<p><b>Enter your WatchYou username and click Send. Your password will be changed, and the new password will be e-mailed to the address on file for your account.</b>
</p>

<p>
<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method="post">
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_RESET_PASSWORD%>' />
<table width=300>
<tr><td align="right">User name:</td><td><input type='text' name='<%=WatchYouCredential.FIELD_USERNAME%>'" /></td></tr>
<tr><td>&nbsp;</td><td align="left"><input type='submit' value='Send' /></td></tr>
</table>
</form>
</p>
</div>


<%@ include file="footer.jsp" %>
