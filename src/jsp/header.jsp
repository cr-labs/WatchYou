<%@ page import="java.text.SimpleDateFormat,com.challengeandresponse.watchyou.*,java.util.Date" %>
<%@ page import="com.challengeandresponse.eventlogger.*" %>


<%	
	WatchYouConfig wyc = (WatchYouConfig) application.getAttribute(WatchYou.SERVLET_CONFIG);
	DBEventLogger sel = (DBEventLogger) application.getAttribute(WatchYou.SERVLET_EVENT_LOGGER); 
	// either of these may be null if the server isn't running or otherwise fails to provide the attribute
	if ((wyc == null) || (sel == null)) {
		out.println("<html><body>The WatchYou server is offline. Please check back later.<br /></body></html>");
		return;
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat(wyc.dateFormatSec);
%>

<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<style type='text/css' media='all'>@import url('<%=wyc.styleSheet%>');</style>

<html>

<head>
<title><%=WatchYou.PRODUCT_LONG%></title>
<script type="text/javascript" 
    src="<%=wyc.scriptLibPath%>/prototype140.js">
</script>
<script type="text/javascript" 
    src="<%=wyc.scriptLibPath%>/rico112.js">
</script>

</head>

<body>
<table width="90%"><tr><td>

<div align='center'><strong><%=WatchYou.VERSION_LONG%></strong><br />
&nbsp;<br />
</div>
