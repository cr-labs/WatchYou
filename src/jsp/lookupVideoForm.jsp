<form action="<%=wyc.jspAbsolutePath%>/youTubeVideo.jsp" method='post'>
YouTube <b>Video ID</b>: <input type='text' name='<%=WatchYou.FIELD_MEDIAID%>' maxlength='<%=WatchYou.MEDIAID_LENGTH_GENERIC%>' 
	size='<%=WatchYou.MEDIAID_LENGTH_GENERIC+3%>' />
<input type='submit' value='Look up video' />
</form>
