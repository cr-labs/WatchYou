package com.challengeandresponse.watchyou;

import javax.servlet.ServletConfig;

import com.challengeandresponse.unomi.server.ServerUtils;

/**
 * key config params for the WatchYou server are in this class
 * 
 * @author jim
 *
 */
public class WatchYouConfig {

	public int maxConsecStopSignals;			// stop tracking a site after N consecutive stop signals
	public int showPingCount;					// last N pings to show for a given tracked video
	
	public String 	mediaDBFile;				// file that holds the video DB
	public String 	eventDBFile;				// file that holds the event DB
	public String	userDBFile;					// file that holds the user DB
	public int		dbMessageLevel;				// message level for DB

	public long		betweenRecordSleepMsec;		// pause N msec between each record in a task
	public long		betweenRoundsSleepMsec;		// pause N msec between passes
	
	public int 		connectTimeoutMsec;
	public int 		readTimeoutMsec;
	public int		serverSecToShow;
	
	public int		sessionMaxInactiveIntervalSec;
	
	
	/**
	 * The format for dates parsed in or formatted for display, including seconds
	 */
	public String 	dateFormat;
	/**
	 * The format for dates parsed in or formatted for display, including seconds
	 */
	public String 	dateFormatSec;
	
	public int		videoWidth;
	public int		videoHeight;
	
	public String	restEndpoint;				// endpoint for REST calls
	public String	userAgent;					// user agent to show on HTTP connections
	
	public String	styleSheet;					// full path to the CSS stylesheet for output from this server
	public String	scriptLibPath;				// path only (no filename, no trailing slash) to javascript libraries
	
	public String	jspAbsolutePath;
	public String	servletAbsolutePath;
	
	public String	authenticationPageUrl;
	
	public String	smtpServer;
	public String	emailFromAccount;
	public String	emailFromName;
	public boolean	checkEmailDomain;
	public int		minDelayBetweenEmailsSec;	// minimum time between automated emails to any user to avoid mailbombs
	
	public String	adminUsername;				// this account must always exist
	public String	initialAdminPassword;		// if account does not exist, it's created with this password
	
	/**
	 * The string to which a video ID is appended, to retrieve that video's web page (we use this to get extended status that the web page shows but that the HTTP service does not provide)<br />
	 * At this writing, the base URL is: http://youtube.com/w/?v=
	 */
	public String 	youTubeVideoBaseUrl;
	public String	youTubeDeveloperID;				// the YouTube developer ID
	
	
	public WatchYouConfig(ServletConfig config) {
		maxConsecStopSignals = ServerUtils.getIntParameter(config,"maxConsecStopSignals",2);
		showPingCount = ServerUtils.getIntParameter(config, "showPingCount", 3);
		
		mediaDBFile = ServerUtils.getStringParameter(config, "mediaDBFile", "/usr/local/unomigear/watchyou/mediadb");
		eventDBFile = ServerUtils.getStringParameter(config, "eventDBFile", "/usr/local/unomigear/watchyou/eventdb");
		userDBFile  = ServerUtils.getStringParameter(config, "userDBFile", "/usr/local/unomigear/watchyou/userdb");
		dbMessageLevel = ServerUtils.getIntParameter(config, "dbMessageLevel", 1);

		betweenRecordSleepMsec = ServerUtils.getIntParameter(config, "betweenRecordSleepMsec", 100);
		betweenRoundsSleepMsec = ServerUtils.getIntParameter(config, "betweenRoundsSleepMsec", 30000);
		
		connectTimeoutMsec = ServerUtils.getIntParameter(config, "connectTimeoutMsec", 20000); 
		readTimeoutMsec = ServerUtils.getIntParameter(config,"readTimeoutMsec",60000);
		serverSecToShow = ServerUtils.getIntParameter(config, "serverSecToShow", 300); 
		
		sessionMaxInactiveIntervalSec = ServerUtils.getIntParameter(config, "sessionMaxInactiveIntervalSec", 1800);
		
		dateFormat = ServerUtils.getStringParameter(config, "dateFormat", "dd MMM yyyy HH:mm z");
		dateFormatSec = ServerUtils.getStringParameter(config, "dateFormatSec", "dd MMM yyyy HH:mm:ss z");

		videoWidth = ServerUtils.getIntParameter(config, "videoWidth", 283);
		videoHeight = ServerUtils.getIntParameter(config, "videoHeight",233);
		
		restEndpoint = ServerUtils.getStringParameter(config, "restEndpoint", "http://www.youtube.com/api2_rest");
		userAgent = ServerUtils.getStringParameter(config, "userAgent", "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.0rc1) Gecko/20020417");
		
		styleSheet = ServerUtils.getStringParameter(config, "styleSheet", "http://watchyou.net/default.css");
		scriptLibPath = ServerUtils.getStringParameter(config, "scriptLibPath", "http://watchyou.net");
		
		youTubeVideoBaseUrl = ServerUtils.getStringParameter(config, "youTubeVideoBaseUrl", "http://youtube.com/w/?v=");
		youTubeDeveloperID = ServerUtils.getStringParameter(config, "youTubeDeveloperID", "");
		
		jspAbsolutePath = ServerUtils.getStringParameter(config, "jspAbsolutePath", "http://localhost:8080/unomigear/WatchYou");
		servletAbsolutePath = ServerUtils.getStringParameter(config, "servletAbsolutePath", "http://localhost:8080/unomigear");
		authenticationPageUrl = ServerUtils.getStringParameter(config, "authenticationPageUrl", "http://watchyou.net/watchyou/jsp/authenticateme.jsp");
		
		smtpServer = ServerUtils.getStringParameter(config, "smtpServer", "");
		emailFromAccount = ServerUtils.getStringParameter(config, "emailFromAccount", "");
		emailFromName = ServerUtils.getStringParameter(config, "emailFromName", "WatchYou Accounts");
		checkEmailDomain = ServerUtils.getBooleanParameter(config, "checkEmailDomain", false);
		minDelayBetweenEmailsSec = ServerUtils.getIntParameter(config, "minDelayBetweenEmailsSec", 300);
		
		adminUsername = ServerUtils.getStringParameter(config, "adminUsername", null);
		initialAdminPassword = ServerUtils.getStringParameter(config, "initialAdminPassword", null);
	}
	
	
}
