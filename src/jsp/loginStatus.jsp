<%
	if (LoginLogout.isLoggedIn(request.getSession())) {
%>
		<b><%=LoginLogout.loggedInUser(request.getSession())%></b>
		&nbsp;|&nbsp;<a href="<%=wyc.servletAbsolutePath%>/WatchYou?<%=WatchYou.ACTION_LABEL%>=<%=WatchYou.ACTION_LOGOUT%>">Log me out</a> 
		&nbsp;|&nbsp;<a href="me.jsp">My info</a><br />
		&nbsp;<br />
<%
	}
	else {
%>	
		<a href="login.jsp">Log me in</a> | <a href="join.jsp">Join WatchYou</a><br />
		&nbsp;<br />
<%
	}
%>
