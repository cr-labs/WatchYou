<% String message = (String) session.getAttribute(WatchYou.SESSION_MESSAGE); 
	// clear the pending message so it doesn't hang around forever
	if (message != null)
		session.removeAttribute(WatchYou.SESSION_MESSAGE);
	out.println( (message != null) ? message+"<br />" : "");
%>
