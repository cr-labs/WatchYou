package com.challengeandresponse.watchyou;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Random;

import javax.servlet.*;
import javax.servlet.http.*;

import com.challengeandresponse.eventlogger.DBEventLogger;
import com.challengeandresponse.eventlogger.EventLoggerException;
import com.challengeandresponse.loginlogout.LoginLogout;
import com.challengeandresponse.loginlogout.LoginLogoutException;
import com.db4o.*;
import com.db4o.config.Configuration;


/**
 * WatchYou is a servlet that monitors YouTube
 * 
 * @author jim
 *
 */
public class WatchYou extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String	PRODUCT_SHORT = "WatchYou";
	public static final String	PRODUCT_LONG = "Challenge/Response WatchYou Server BETA";
	public static final String	VERSION_SHORT = "0.45";
	public static final String	VERSION_LONG = PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT = "Copyright (c) 2006-2007 Challenge/Response LLC, Cambridge, MA";
	
	public static final String	ACTION_LABEL = "action";
	public static final String	ACTION_ADD_VIDEO = "add";
	public static final String 	ACTION_ADD_USER = "adduser";
	public static final String	ACTION_UPDATE_USER = "updateuser";
	public static final String	ACTION_VALIDATE_USER = "validateuser";
	public static final String	ACTION_RESEND_VALIDATION = "resendvalidation";
	public static final String	ACTION_RESET_PASSWORD = "resetpassword";
	public static final String	ACTION_CHANGE_PASSWORD = "changepassword";
	public static final String 	ACTION_GET_THUMBNAIL_IMAGE = "image";
	public static final String	ACTION_LOGIN = "login";
	public static final String	ACTION_LOGOUT = "logout";

	// fields on the forms
	public static final String	FIELD_MEDIAID	= "mediaid";
	public static final int		MEDIAID_LENGTH_GENERIC = 11;

	// SHARED OBJECTS
	// APPLICATION (SERVLET) LEVEL OBJECTS, access with application.getAttribute() in servlets
	public static final String	SERVLET_CONFIG  		= PRODUCT_SHORT+":config";
	public static final String	SERVLET_LOGINLOGOUT 	= PRODUCT_SHORT+":loginlogout";
	public static final String	SERVLET_EVENT_LOGGER	= PRODUCT_SHORT+":logger";
	
	// SESSION OBJECTS, access with session.getAttribute() in servlets
	public static final String	SESSION_YTR 		= PRODUCT_SHORT+":session:ytr";
	public static final String	SESSION_YTRIO 		= PRODUCT_SHORT+":session:ytrio";
	public static final String	SESSION_MESSAGE 	= PRODUCT_SHORT+":session:message";
	public static final String	SESSION_CREDENTIAL 	= PRODUCT_SHORT+":session:credential";
	
	// JSP PAGES WHOSE NAMES HAVE TO BE KNOWN ALL OVER FOR REDIRECTS
	public static final String	ABOUT_PAGE 	= "about.jsp";
	public static final String	HOME_PAGE 	= "home.jsp";
	public static final String	LOGIN_PAGE 	= "login.jsp";
	public static final String	JOIN_PAGE 	= "join.jsp";
	public static final String	ME_PAGE 	= "me.jsp";
	public static final String	VIDEO_PAGE 	= "youTubeVideo.jsp";
	public static final String	AUTH_PAGE	= "authenticateme.jsp";
	
	
	private static final int AUTHMESSAGE_ADD = 1;
	private static final int AUTHMESSAGE_UPDATE = 2;
	private static final int AUTHMESSAGE_RESEND = 3;

	
	// server config
	private static WatchYouConfig wyc;

	// the media database server
	private static ObjectServer mediaDBServer;

	// login/logout session management and user handling
	private static LoginLogout lilo;
	// server event logger
	private static DBEventLogger dbel;
	
	// media monitor threads
	private MediaMonitor mm1;
	
	// server stats
	private static long 		serverStartTime;
	
	// the YouTube REST interface
	private static YouTubeREST 	ytRest;
	
	// SMTP access 
	private static SimpleMail	smail;
	
	// a source of randomness that other things can use
	private static Random		random;

	public WatchYou() {
		super();
	}

	// Load everything that's held for the life of the servlet instance
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println("Server starting: "+VERSION_LONG);
		serverStartTime = System.currentTimeMillis();

		System.out.println("Loading WatchYouConfig");
		wyc = new WatchYouConfig(config);
		
		System.out.println("Starting EventLogger");
		try {
			dbel = new DBEventLogger(wyc.eventDBFile,wyc.dbMessageLevel);
		} 
		catch (EventLoggerException ele) {
			throw new ServletException(ele.getMessage());
		}
		dbel.addEvent("EventLogger started");

		dbel.addEvent("Initializing YouTubeREST to endpoint: "+wyc.restEndpoint);
		try {
			ytRest = new YouTubeREST(wyc.restEndpoint,wyc);
		} 
		catch(MalformedURLException mue) {
			throw new ServletException("Exception setting REST endpoint to "+wyc.restEndpoint+":"+mue.getMessage());
		}

		dbel.addEvent("Opening SimpleMail, server:"+wyc.smtpServer+" account:"+wyc.emailFromName+" <"+wyc.emailFromAccount+">");
		smail = new SimpleMail(wyc.smtpServer,wyc.emailFromName,wyc.emailFromAccount);

		dbel.addEvent("Initializing random number source");
		random = new Random();

		System.out.println("Opening Login/Logout Manager");
		try {
			// check for default account IF one is indicated in the config (username and initial password required)
			if ((wyc.adminUsername != null) && (wyc.initialAdminPassword != null)) {
				WatchYouCredential da = new WatchYouCredential(wyc.adminUsername);
				da.setPassword(wyc.initialAdminPassword);
				da.setFlag(WatchYouCredential.TOKEN_MANAGER);
				da.setFlag(WatchYouCredential.TOKEN_EMAIL_VALIDATED);
				dbel.addEvent("Will check for default account and create it if necessary:"+wyc.adminUsername);
				lilo = new LoginLogout(wyc.userDBFile,wyc.dbMessageLevel,da);
				if (lilo.createdDefaultAccount())
					System.out.println("Default account: "+wyc.adminUsername+" with default password, has been created.");
			}
			else { // otherwise do not test for default account, just start up
				dbel.addEvent("Will not check for default account");
				lilo = new LoginLogout(wyc.userDBFile,wyc.dbMessageLevel);
			}
			lilo.registerDenyLoginFlag(WatchYouCredential.TOKEN_EMAIL_SENT_AWAITING_VALIDATION, "You must authenticate your e-mail address before you can log in. Check your inbox for a message with instructions for authenticating the address.");
			lilo.registerDenyLoginFlag(WatchYouCredential.TOKEN_EMAIL_NOT_SENT_AWAITING_RETRY, "You must authenticate your e-mail address before you can log in. We have not yet succeeded in sending a validation e-mail to you. Please watch for our message in your inbox.");
			lilo.registerAllowLoginFlag(WatchYouCredential.TOKEN_EMAIL_VALIDATED);
		} 
		catch (LoginLogoutException liloe) {
			throw new ServletException(liloe.getMessage());
		}
		
		// open video database, first setting the configuration to be applied when they are opened
		Configuration conf = Db4o.newConfiguration();
		conf.allowVersionUpdates(true);
		conf.messageLevel(wyc.dbMessageLevel);
		conf.objectClass(MediaRecord.class).objectField("mediaID").indexed(true);
		conf.objectClass("com.challengeandresponse.watchyou.MediaRecord").cascadeOnActivate(true);
		conf.objectClass("com.challengeandresponse.watchyou.MediaRecord").cascadeOnUpdate(true);
		conf.objectClass("com.challengeandresponse.watchyou.MediaRecord").cascadeOnDelete(true);
		conf.activationDepth(3);
		conf.updateDepth(3);
		try {
			dbel.addEvent("Opening videoDB server on file "+wyc.mediaDBFile);
			mediaDBServer = Db4o.openServer(conf,wyc.mediaDBFile,0);
		}
		catch (com.db4o.ext.Db4oException db4oe) {
			throw new ServletException("Exception opening videoDB or userDB:"+db4oe.getMessage());
		}
		
		// publish the shared objects 
		dbel.addEvent("Publishing shared objects");
		getServletContext().setAttribute(SERVLET_CONFIG,wyc);
		getServletContext().setAttribute(SERVLET_LOGINLOGOUT,lilo);
		getServletContext().setAttribute(SERVLET_EVENT_LOGGER,dbel);

		// start the media monitor
		dbel.addEvent("Instantiating MediaMonitor");
		mm1 = new MediaMonitor(
				"MM1",
				 new MediaRecordIO(getMediaDBObjectContainer(),mediaDBServer.ext().objectContainer()),
				 wyc.betweenRecordSleepMsec,
				 wyc.betweenRoundsSleepMsec,
				 dbel);
		dbel.addEvent("Starting media monitor thread");
		mm1.start();
		
		dbel.addEvent("Completed Initializing. Server running: "+VERSION_LONG);
		System.out.println("Completed Initializing. Server running: "+VERSION_LONG);
		
		// give the Media Monitor something to do... this is a repeating task to fetch status for all records
		MMUpdateAllRecords mmau = new MMUpdateAllRecords(ytRest,dbel);
		mm1.addRepeatingTask(mmau);
	}
	
	
	public void destroy() {
		mm1.shutdown();			// media monitor
		dbel.shutdown();			// server event logger
		lilo.shutdown();		// loginlogout manager
		mediaDBServer.close();
	}

	

	/**
	 * Handles HTTP <code>GET</code>, the only method supported here for now
	 * @param _request servlet request
	 * @param _response servlet response
	 * @param message an optional message to display after the header, on the home page (used for internal calls that call doGet to just force the user back to the main page) Make this null to display no message.
	 */
	public void doGet(HttpServletRequest _request,HttpServletResponse _response)
	throws ServletException, IOException {
		String action = _request.getParameter(ACTION_LABEL);
		dbel.addEvent(_request,"GET request, action="+action);
		mediaRecordIOFactory(_request,"doGet"); // bind a YouTubeRecordIO instance to this session if there isn't one already
		
		// IMAGE: if a session is opened then there's a ytr bound to it, deliver the cached thumbnail if there is one
		if (ACTION_GET_THUMBNAIL_IMAGE.equals(action)) {
			// we need to be in a session to do this
			YouTubeRecord ytr = (YouTubeRecord) _request.getSession().getAttribute(SESSION_YTR);
			if ((ytr != null) && (ytr.cachedThumbnail != null)) {
				dbel.addEvent(_request,"Answering with cached thumbnail from session-linked YouTubeRecord");
				_response.setContentType(ytr.thumbnailContentType);
				ServletOutputStream outStream = _response.getOutputStream();
				outStream.write(ytr.cachedThumbnail);
				outStream.close();
			}
			else {
				dbel.addEvent(_request,"No session, or cached thumbnail was null; sending SC_NOT_FOUND error");
				_response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		// LOG OUT
		else if (ACTION_LOGOUT.equals(action)) {
			dbel.addEvent(_request,"Logging out: "+LoginLogout.loggedInUser(_request.getSession()));
			LoginLogout.doLogout(_request.getSession());
			_request.getSession().setAttribute(SESSION_MESSAGE,"Logged out");
			_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
		}
		// OTHER: all other GETs just get redirected to the home page
		else {
			_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
		}
	}

	
	
	public void doPost(HttpServletRequest _request,HttpServletResponse _response)
	throws ServletException, IOException {
		String action = _request.getParameter(ACTION_LABEL);
		dbel.addEvent(_request,"POST request, action="+action);
		mediaRecordIOFactory(_request,"doPost"); // bind a YouTubeRecordIO instance to this session if there isn't one already

		// LOGIN
		if (ACTION_LOGIN.equals(action)) {
			String assertedUser = WatchYouUtils.disableAndTrimTags(_request.getParameter(WatchYouCredential.FIELD_USERNAME));
			String assertedPassword = _request.getParameter(WatchYouCredential.FIELD_PASSWORD).trim();
			WatchYouCredential c = null;
			try {
				c = (WatchYouCredential) lilo.doLogin(assertedUser, assertedPassword, _request.getSession());
				dbel.addEvent(_request,"Logged in: "+assertedUser);
				_request.getSession().setMaxInactiveInterval(wyc.sessionMaxInactiveIntervalSec);
				_request.getSession().setAttribute(SESSION_MESSAGE,"Login successful. Hello "+c.username);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				return;
			}
			catch (LoginLogoutException liloe) {
				String message = "Could not login: "+liloe.getMessage();
				String destination = LOGIN_PAGE;
				// if flags stopped this and auth is required, go to the auth page instead of back to the login page
				if ( (liloe.getCode() == LoginLogout.ERROR_LOGIN_DENIED_BY_FLAGS_GENERAL) && (message.indexOf("You must authenticate") > -1) )
						destination = AUTH_PAGE;
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+destination);
				return;
			}
		} 
		// ADD A VIDEO
		else if (ACTION_ADD_VIDEO.equals(action)) {
			// must be logged in to add a video
			if (! LoginLogout.isLoggedIn(_request.getSession())) {
				dbel.addEvent(_request,"Attempted ADD_VIDEO but session is not logged in");
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+LOGIN_PAGE);
				return;
			}
			YouTubeRecord ytr = (YouTubeRecord) _request.getSession().getAttribute(SESSION_YTR);
			MediaRecordIO mrio = (MediaRecordIO) _request.getSession().getAttribute(SESSION_YTRIO);
			if ( (ytr != null) && (mrio != null)) {
				ytr.addedByUser = LoginLogout.loggedInUser(_request.getSession());
				if (mrio.addMediaRecord(ytr)) {
					// 1. Note this event; 2. set the response message; 3. make sure the browser session has the correct video bound to it
					dbel.addEvent(_request,"Add video request; successfully added: "+ytr.mediaID);
					_request.getSession().setAttribute(SESSION_MESSAGE,"The video <b>"+ytr.mediaID+"</b> is now tracked");
					_request.getSession().setAttribute(SESSION_YTR,ytr);
				}
				else {
					// 1. Note this event; 2. set the response message; 3. make sure the browser session has the correct video bound to it
					dbel.addEvent(_request,"Add video request; video is already tracked: "+ytr.mediaID);
					_request.getSession().setAttribute(SESSION_MESSAGE,"The video <b>"+ytr.mediaID+"</b> is already tracked");
					_request.getSession().setAttribute(SESSION_YTR,ytr);
				}
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+VIDEO_PAGE+"?"+FIELD_MEDIAID+"="+ytr.mediaID);
				return;
			}
			else {
				// 1. Note this event; 2. set the response message; 3. unbind all YTRs from the session
				dbel.addEvent(_request,"Add video request; YouTubeRecord and/or YouTubeRecordIO not found in session");
				_request.getSession().setAttribute(SESSION_MESSAGE,"Internal error. YouTubeRecord or YouTubeRecordIO not found. Cannot set up tracking.");
				_request.getSession().removeAttribute(SESSION_YTR);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				return;
			}
		}
		// ADD A USER
		else if (ACTION_ADD_USER.equals(action)) {
			dbel.addEvent(_request,"add user");
			// we may have set a temp SESSION_CREDENTIAL to deal with could-not-create error on new account. remove it
			_request.getSession().removeAttribute(SESSION_CREDENTIAL);
			// marshall the fields from the form
			WatchYouCredential c = null;
			try {
				c = new WatchYouCredential( _request.getParameter(WatchYouCredential.FIELD_USERNAME).trim());
				c.email = _request.getParameter(WatchYouCredential.FIELD_EMAIL).trim();
				c.youTubeUserId = _request.getParameter(WatchYouCredential.FIELD_YOUTUBEUSERNAME).trim();
				String plainPassword = _request.getParameter(WatchYouCredential.FIELD_PASSWORD).trim();
				String plainPassword2 = _request.getParameter(WatchYouCredential.FIELD_PASSWORD2).trim();
				if (! plainPassword.equals(plainPassword2))
					throw new LoginLogoutException("The passwords don't match. Be sure to type the same password in both boxes");
				// check all fields for sufficient content... list out complaints if not
				String message = c.complain(plainPassword,wyc.checkEmailDomain);
				if (message.length() > 0)
					throw new LoginLogoutException(message);
				// otherwise add the record, failing if there is a duplicate username
				else {
					c.setPassword(plainPassword);
					lilo.addUser(c);
					message="Your new account has been created. Welcome, "+c.username+
						" Check your e-mail for an important account activation message from WatchYou.net";
					dbel.addEvent(_request,message);
					_request.getSession().setAttribute(SESSION_MESSAGE,message);
					_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
					c.setAuthenticationKey();
					if (sendAuthenticationEmail(AUTHMESSAGE_ADD,c)) {
						c.setFlag(WatchYouCredential.TOKEN_EMAIL_SENT_AWAITING_VALIDATION);
						dbel.addEvent("Sent new account email confirmation to "+c.email+" for "+c.username);
					}
					else {
						c.setFlag(WatchYouCredential.TOKEN_EMAIL_NOT_SENT_AWAITING_RETRY);
						dbel.addEvent("Could not send new account email confirmation to "+c.email+" for "+c.username);
					}
					lilo.updateUser(c);
					return;
				}
			}
			catch (LoginLogoutException liloe) {
				String message="Could not create account: "+liloe.getMessage();
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_request.getSession().setAttribute(SESSION_CREDENTIAL,c);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+JOIN_PAGE);
				return;
			}
		} // end of ADD A USER
		// UPDATE A USER
		else if (ACTION_UPDATE_USER.equals(action)) {
			dbel.addEvent(_request,"update user");
			String message;
			// must be logged in to update a user record
			if (! LoginLogout.isLoggedIn(_request.getSession())) {
				dbel.addEvent(_request,"Attempted UPDATE_USER but session is not logged in");
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+LOGIN_PAGE);
				return;
			}
			// retrieve the user object, then overlay the editable fields
			WatchYouCredential c = null;
			try {
				c = (WatchYouCredential) lilo.getUser(LoginLogout.loggedInUser(_request.getSession()));
				if (c == null)
					throw new LoginLogoutException("User does not exist:"+c.username,LoginLogout.ERROR_USERNAME_NOT_FOUND);
				String originalEmail = c.email;
				c.email = _request.getParameter(WatchYouCredential.FIELD_EMAIL).trim();
				c.youTubeUserId = _request.getParameter(WatchYouCredential.FIELD_YOUTUBEUSERNAME).trim();
				message = c.complain(wyc.checkEmailDomain);
				// if the data weren't good enough, try again
				if (message.length() > 0) {
					dbel.addEvent(_request,message);
					_request.getSession().setAttribute(SESSION_MESSAGE,message);
					_request.getSession().setAttribute(SESSION_CREDENTIAL,c);
					_response.sendRedirect(wyc.jspAbsolutePath+"/"+ME_PAGE);
					return;
				}
				lilo.updateUser(c);
				message = "User info updated for "+c.username;
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				// if email has changed, send a new authentication message, first clearing all email-related validation/auth pending flags
				if (! originalEmail.equals(c.email)) {
					c.setAuthenticationKey();
					if (sendAuthenticationEmail(AUTHMESSAGE_UPDATE,c)) {
						c.unsetFlags(WatchYouCredential.EMAIL_VALIDATION_TOKENS);
						c.setFlag(WatchYouCredential.TOKEN_EMAIL_SENT_AWAITING_VALIDATION);
						c.touchAuthkeyLastSent();
						dbel.addEvent("Sent account update email to "+c.email+" for "+c.username);
					}
					else {
						c.unsetFlags(WatchYouCredential.EMAIL_VALIDATION_TOKENS);
						c.setFlag(WatchYouCredential.TOKEN_EMAIL_NOT_SENT_AWAITING_RETRY);
						dbel.addEvent("Could not send account update email to "+c.email+" for "+c.username);
					}
					// update newly account to catch flags based on e-mailing
					lilo.updateUser(c);
				}
				return;
			}
			catch (LoginLogoutException liloe) {
				message = "Could not update user info: "+liloe.getMessage();
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_request.getSession().removeAttribute(SESSION_CREDENTIAL);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+ME_PAGE);
				return;
			}
		} // end of UPDATE A USER
		// VALIDATE A USER -- username + auth code that we sent by email
		else if (ACTION_VALIDATE_USER.equals(action)) {
			String userID =  _request.getParameter(WatchYouCredential.FIELD_USERNAME).trim();
			String authcode = _request.getParameter(WatchYouCredential.FIELD_AUTHCODE).trim();
			String message;
			try {
				if ((userID == null) || (authcode == null))
					throw new LoginLogoutException("userID and authcode are both needed");
				WatchYouCredential c = (WatchYouCredential) lilo.getUser(userID);
				if (c == null)
					throw new LoginLogoutException("The user ID you entered was not found: "+userID);
				// account should not already be authenticated
				if (c.hasFlag(WatchYouCredential.TOKEN_EMAIL_VALIDATED)) {
					message = "This account has already been validated: "+userID+" Go ahead and log in!";
					dbel.addEvent(_request,message);
					_request.getSession().setAttribute(SESSION_MESSAGE,message);
					_response.sendRedirect(wyc.jspAbsolutePath+"/"+LOGIN_PAGE);
					return;
				}
				if (c.validateAuthenticationKey(Integer.parseInt(authcode))) {
					c.unsetFlags(WatchYouCredential.EMAIL_VALIDATION_TOKENS);
					c.setFlag(WatchYouCredential.TOKEN_EMAIL_VALIDATED);
					lilo.updateUser(c);
					message = "Your e-mail address is authenticated and now you can log in. Thanks for being a part of WatchYou.net!";
					dbel.addEvent(_request,message);
					_request.getSession().setAttribute(SESSION_MESSAGE,message);
					_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
					return;
				}
				else {
					throw new LoginLogoutException("That isn't the correct authentication code for this user ID");
				}
			}
			catch (LoginLogoutException liloe) {
				message = "Could not process your authentication attempt: "+liloe.getMessage();
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+AUTH_PAGE);
				return;
			}
			catch (NumberFormatException nfe) {
				message = "Could not process your authentication attempt. That isn't an authentication code";
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+AUTH_PAGE);
				return;
			}
		}
		// RESEND VALIDATION TO AN UNVALIDATED USER
		else if (ACTION_RESEND_VALIDATION.equals(action)) {
			// marshall the fields from the form
			String userID = _request.getParameter(WatchYouCredential.FIELD_USERNAME).trim();
			String message;
			try {
				WatchYouCredential c = (WatchYouCredential) lilo.getUser(userID);
				if (c == null)
					throw new LoginLogoutException("Can't find the user ID: "+userID);
				// account must not already be authenticated
				if (c.hasFlag(WatchYouCredential.TOKEN_EMAIL_VALIDATED)) {
					message = "This account has already been validated: "+userID+" Go ahead and log in!";
					dbel.addEvent(_request,message);
					_request.getSession().setAttribute(SESSION_MESSAGE,message);
					_response.sendRedirect(wyc.jspAbsolutePath+"/"+LOGIN_PAGE);
					return;
				}
				// can't be too soon since the last attempt
				if ( (c.getAuthkeyLastSent()+(wyc.minDelayBetweenEmailsSec*1000l)) > System.currentTimeMillis())
					throw new LoginLogoutException("We recently e-mailed a copy of the validation code for the account: "+userID+" Please check your inbox, then try again later if you still need it.");
				// try to send the code by email
				if (sendAuthenticationEmail(AUTHMESSAGE_RESEND,c)) {
					c.unsetFlags(WatchYouCredential.EMAIL_VALIDATION_TOKENS);
					c.setFlag(WatchYouCredential.TOKEN_EMAIL_SENT_AWAITING_VALIDATION);
					c.touchAuthkeyLastSent();
					message = "Sent the authentication code to the e-mail address on file for "+c.username;
					dbel.addEvent(message);
				}
				else {
					c.unsetFlags(WatchYouCredential.EMAIL_VALIDATION_TOKENS);
					c.setFlag(WatchYouCredential.TOKEN_EMAIL_NOT_SENT_AWAITING_RETRY);
					message = "Could not send the authentication code via email to the e-mail address on file for "+c.username;
					dbel.addEvent(message);
				}
				// update the user record with new details from above
				lilo.updateUser(c);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				return;
			}
			catch (LoginLogoutException liloe) {
				message = "Couldn't update the account: "+liloe.getMessage();
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				return;
			}
		}
		// RESET PASSWORD AND MAIL IT TO THE USER
		else if (ACTION_RESET_PASSWORD.equals(action)) {
			// marshall the fields from the form
			String userID = _request.getParameter(WatchYouCredential.FIELD_USERNAME).trim();
			try {
				WatchYouCredential c = (WatchYouCredential) lilo.getUser(userID);
				if (c == null)
					throw new LoginLogoutException("Can't find the user ID: "+userID);
				// can't be too soon since the last attempt
				if ( (c.getAuthkeyLastSent()+(wyc.minDelayBetweenEmailsSec*1000l)) > System.currentTimeMillis())
					throw new LoginLogoutException("We recently e-mailed correspondence for the account: "+userID+" and can't send more mail right away. Please make this request later.");
				// reset the password and try to send it by e-mail
				int pw = getNextRandomInt();
				c.setPassword(pw+"");
				if (sendEmail("Your new WatchYou.net password","Your WatchYou.net password has been changed to: "+pw+"\n",c)) {
					c.touchAuthkeyLastSent();
					lilo.updateUser(c);
					String message = "Sent the new password to the e-mail address on file for "+c.username;
					dbel.addEvent(message);
					_request.getSession().setAttribute(SESSION_MESSAGE,message);
					_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
					return;
				}
				else {
					throw new LoginLogoutException("Could not send the new password via email to the e-mail address on file for "+c.username);
				}
			}
			catch (LoginLogoutException liloe) {
				String message = "An error occurred while resetting and sending the password for: "+userID+" "+liloe.getMessage();
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				return;
			}
		}

		// CHANGE PASSWORD
		else if (ACTION_CHANGE_PASSWORD.equals(action)) {
			// must be logged in to change a password
			if (! LoginLogout.isLoggedIn(_request.getSession())) {
				dbel.addEvent(_request,"Attempted CHANGE_PASSWORD but session is not logged in");
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+LOGIN_PAGE);
				return;
			}
			// retrieve the user object, then overlay the editable fields
			WatchYouCredential c = null;
			try {
				c = (WatchYouCredential) lilo.getUser(LoginLogout.loggedInUser(_request.getSession()));
				if (c == null)
					throw new LoginLogoutException("User does not exist:"+c.username,LoginLogout.ERROR_USERNAME_NOT_FOUND);
				String plainPassword	= _request.getParameter(WatchYouCredential.FIELD_PASSWORD).trim();
				String plainPassword2	= _request.getParameter(WatchYouCredential.FIELD_PASSWORD2).trim();
				if (! plainPassword.equals(plainPassword2))
					throw new LoginLogoutException("The passwords don't match. Be sure to type the same password in both boxes");
				// see if the password is acceptable
				String message = c.complain(plainPassword);
				if (message.length() > 0)
					throw new LoginLogoutException(message);
				// otherwise add the record, failing if there is a duplicate username
				c.setPassword(plainPassword);
				lilo.updateUser(c);
				message = "Password has been changed for "+c.username;
				dbel.addEvent(message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
				return;
			}
			catch (LoginLogoutException liloe) {
				String message = "Could not change password for user: "+c.username+": "+liloe.getMessage();
				dbel.addEvent(_request,message);
				_request.getSession().setAttribute(SESSION_MESSAGE,message);
				_response.sendRedirect(wyc.jspAbsolutePath+"/"+ME_PAGE);
				return;
			}
		} // end of CHANGE PASSWORD
		
		
		
		// OTHER: all other POSTs are just redirected to the home page
		else {	
			_response.sendRedirect(wyc.jspAbsolutePath+"/"+HOME_PAGE);
			return;
		}
	}


	

	/**
	 * Convenience method using the server's YouTubeRest facility to fetch a YouTubeRecord from YouTube
	 * @param videoID the video ID to fetch
	 * @return the YouTubeRecord with initial details all filled in, or null if the video is not in in the database
	 */
	public static YouTubeRecord getYouTubeRecordFromSite(String videoID)
	throws WatchYouException {
		return ytRest.fetchVideoById(videoID,true);
	}

	/**
	 * Convenience method for any session to getch a MediaRecord from the database, if there is one, using the session's YouTubeRecordIO
	 * if there is one, or creating a YouTubeRecordIO instance if the session lacks one
	 * @param videoID	The videoID to fetch
	 * @param request	The YouTubeRecord
	 * @return
	 */
	public static MediaRecord getMediaRecord(String mediaID, HttpServletRequest request) {
		MediaRecordIO mrio = mediaRecordIOFactory(request,"getMediaRecord");
		return mrio.getMediaRecord(mediaID);
	}
	
	/**
	 * Convenience method for JSPs to call back
	 * @param prototype
	 * @param session
	 * @return
	 */
	public static List<MediaRecord> getMediaRecords(MediaRecord prototype,HttpServletRequest request) {
		MediaRecordIO mrio = mediaRecordIOFactory(request,"getMediaRecords");
		return mrio.getMediaRecords(prototype);
	}
	
	
	/**
	 * Make a MediaRecordIO and stuff it into the session connected to this request, IFF the sessions doesn't have one already
	 * @param session
	 * @param caller
	 * @return
	 */
	private static MediaRecordIO mediaRecordIOFactory(HttpServletRequest request, String caller) {
		MediaRecordIO mrio = (MediaRecordIO) request.getSession().getAttribute(SESSION_YTRIO);
		if (mrio == null) {
			dbel.addEvent(request,caller+":Creating new YouTubeRecordIO for session"+request.getSession().getId());
			mrio = new MediaRecordIO(getMediaDBObjectContainer(),mediaDBServer.ext().objectContainer());
			request.getSession().setAttribute(SESSION_YTRIO,mrio);
		}
		return mrio;
	}
	
	/**
	 * Just delivers a new ObjectContainer on the mediaDB server
	 * @return a new ObjectContainer on the mediaDB server
	 */
	private static ObjectContainer getMediaDBObjectContainer() {
		ObjectContainer oc = mediaDBServer.openClient();
		oc.ext().configure().allowVersionUpdates(true);
		oc.ext().configure().messageLevel(wyc.dbMessageLevel);
		oc.ext().configure().objectClass(MediaRecord.class).objectField("mediaID").indexed(true);
		oc.ext().configure().objectClass("com.challengeandresponse.watchyou.MediaRecord").cascadeOnActivate(true);
		oc.ext().configure().objectClass("com.challengeandresponse.watchyou.MediaRecord").cascadeOnUpdate(true);
		oc.ext().configure().objectClass("com.challengeandresponse.watchyou.MediaRecord").cascadeOnDelete(true);
		oc.ext().configure().activationDepth(3);
		oc.ext().configure().updateDepth(3);
		return oc;
	}
	

	
	/**
	 * Convenience method for any session to get a User from the database, if there is one, using the server's LoginLogout
	 * @param request	The HTTP request of the session having a logged in user
	 * @return the WatchYouCredential of the currently logged in user for this session, or null if no match
	 */
	public static WatchYouCredential getLoggedInUserRecord(HttpServletRequest request) {
		return (WatchYouCredential) lilo.getUser(LoginLogout.loggedInUser(request.getSession()));
	}

	
	
	public static long getUptimeMillis() {
		return (System.currentTimeMillis() - serverStartTime);
	}
	
	/**
	 * Generates the text and tries to send an authentication for an e-mail address.
	 * This is implemented as a method so that the e-mail can be 
	 * sent from different places in the code such as:<br />
	 * 1. Adding a new account<br />
	 * 2. Changing the e-mail address of an account<br />
	 * 3. Requesting a re-send of the authentication code<br />
	 * <br />
	 * Valid message types are:
	 * AUTHMESSAGE_ADD<br />
	 * AUTHMESSAGE_UPDATE<br />
	 * AUTHMESSAGE_RESEND<br />
	 */
	private boolean sendAuthenticationEmail(int type, WatchYouCredential c) {
		String u = wyc.authenticationPageUrl+"?"+WatchYouCredential.FIELD_USERNAME+"="+c.username+"&"+WatchYouCredential.FIELD_AUTHCODE+"="+c.getAuthenticationKey();
		switch (type) {
		case AUTHMESSAGE_ADD:
			return sendEmail("WatchYou.net new account confirmation",
					"Hi and welcome to WatchYou!\nBelow are your account confirmation details.\n"+
					"Your username is: "+c.username+"\nYour authentication code is:"+c.getAuthenticationKey()+"\n"+
					"To activate your account go to "+u+"\n"
					,c);
		case AUTHMESSAGE_UPDATE:
			return sendEmail("WatchYou.net email address update confirmation",
					"Hi. Because you changed the e-mail address for "+c.username+" we need to reconfirm it.\n"+ 
					"Your username is: "+c.username+"\nYour authentication code is:"+c.getAuthenticationKey()+"\n"+
					"To activate your account go to "+u+"\n"
					,c);
		case AUTHMESSAGE_RESEND:
			return sendEmail("Your WatchYou.net authentication code",
					"Hi. As requested from the WatchYou web site, here is the authentication code for your account "+c.username+".\n"+
					"Your username is: "+c.username+"\nYour authentication code is:"+c.getAuthenticationKey()+"\n"+
					"To activate your account go to "+u+"\n"
					,c);
		default:
			return false;
		}
	}

	
	/**
	 * Send a message to a user (probably an authentication message) using the SimpleMail class as the mailer2
	 * @param subject
	 * @param message
	 * @param c
	 * @return
	 */
	private boolean sendEmail(String subject, String message, WatchYouCredential c) {
		try {
		 smail.sendMail(c.email, subject, message);
		}
		catch (IOException ioe) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * @return the next random long (made positive with Math.abs() ) from the servlet's initialized random number generator
	 */
	public static int getNextRandomInt() {
		return Math.abs(random.nextInt());
	}


	
} // end of class WatchYou
