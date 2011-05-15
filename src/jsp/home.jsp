<%@ page import="java.util.*,com.challengeandresponse.watchyou.*" %>

<%@ include file="header.jsp" %>
<% sel.addEvent(request,"home.jsp"); %>

<div align='center'>

<%@ include file="lookupVideoForm.jsp" %>
&nbsp;<br />
<%@ include file="showAndClearMessage.jsp" %>
<%@ include file="loginStatus.jsp" %>
</div>

<%@ include file="footer.jsp" %>
