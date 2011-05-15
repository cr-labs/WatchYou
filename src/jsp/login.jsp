<%@ page import="java.util.*,com.challengeandresponse.watchyou.*" %>

<% //  page is not available if user is logged in
	if (LoginLogout.isLoggedIn(session)) {
		response.sendRedirect(WatchYou.HOME_PAGE);
		return;
	}
%>


<%@ include file="header.jsp" %>
<% sel.addEvent(request,"login.jsp"); %>

<div align="center">
<%@ include file="showAndClearMessage.jsp" %>

<form action="<%=wyc.servletAbsolutePath%>/WatchYou" method="post">
<input type='hidden' name='<%=WatchYou.ACTION_LABEL%>' value='<%=WatchYou.ACTION_LOGIN%>' />
User name: <input type='text' name='<%=WatchYouCredential.FIELD_USERNAME%>' size="16" maxlength="15">&nbsp;
Password: <input type='password' name='<%=WatchYouCredential.FIELD_PASSWORD%>' size="10" maxlength="15" />&nbsp;
<input type='submit' value='Log me in' />
</form>
&nbsp;
<a href="resetPassword.jsp">Forgot my password</a><br />
</div>

<%@ include file="footer.jsp" %>
