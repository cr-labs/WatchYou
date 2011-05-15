package com.challengeandresponse.watchyou;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.challengeandresponse.loginlogout.Credential;

/**
 * Extends Credential to include additional fields for WatchYou users
 * @author jim
 */


public class WatchYouCredential extends Credential {

	/** User ID in YouTube */
	public String	youTubeUserId;
	/** The key used to authenticate a new or changed e-mail address, a random integer */
	private int		authenticationKey;
	/** Timestamp when last authentication message was sent - can be used to avoid mail bombs */
	private long	authkeyLastSent;


	public static final int		MIN_USERNAME_LENGTH = 2;
	public static final int		MIN_PASSWORD_LENGTH = 3;


	// field name used in JSPs that reference WatchYouCredential objects on forms
	public static final String	FIELD_USERNAME	= "username";
	public static final String 	FIELD_PASSWORD	= "password";
	public static final String 	FIELD_PASSWORD2	= "password2";
	public static final String	FIELD_EMAIL		= "email";
	public static final String	FIELD_AUTHCODE	= "authcode";
	public static final String	FIELD_YOUTUBEUSERNAME	= "youtubeusername";

	public static final char	TOKEN_EMAIL_NOT_SENT_AWAITING_RETRY = 'x';
	public static final char	TOKEN_EMAIL_SENT_AWAITING_VALIDATION = 'e';
	public static final char	TOKEN_EMAIL_VALIDATED = 'E';
	public static final char	TOKEN_MANAGER = 'M';
	
	public static final Character[] EMAIL_VALIDATION_TOKENS = new Character[] {
		TOKEN_EMAIL_NOT_SENT_AWAITING_RETRY,
		TOKEN_EMAIL_SENT_AWAITING_VALIDATION,
		TOKEN_EMAIL_VALIDATED
	};


	/**
	 * Constructor. Call with userid=null to get an object WITHOUT 
	 * default values loaded, for use in QBE queries
	 * @param userid the userid to put in this credential, or null to get an uninitialized object for QBE usage
	 */
	public WatchYouCredential(String userid) {
		super(userid);
		if (userid != null) {
			youTubeUserId = "";
			authenticationKey = 0;
			authkeyLastSent = 0;
		}
	}


	/**
	 * Check the fields of this WatchYouCredential for satisfactory content.
	 * Use the complain(String plainPassword) to ALSO check a plaintext password
	 * @maram checkEmailDomain	if true, the domain part of the e-mail address will be checked in DNS to be sure it exists
	 * @return an empty string if there were no problems, or a text message listing one or more problems
	 */
	String complain(boolean checkEmailDomain) {
		StringBuffer errorMessage = new StringBuffer();
		// check the username
		if (username.length() < MIN_USERNAME_LENGTH)
			errorMessage.append("Username should be at least "+MIN_USERNAME_LENGTH+" characters long. ");
		// check the email address
		if ( (email.length() < 1) || (email.indexOf("@") < 1) || (email.lastIndexOf(".") < email.lastIndexOf("@")) 
				|| (email.lastIndexOf(".") ==  email.length()-1) || (email.indexOf(" ") > -1))
			errorMessage.append("A valid e-mail address is needed - this is how we contact you about your account.");
		//	  if the format of the address was ok, confirm that it's for a real domain that can be looked up
		else if (checkEmailDomain) { 
			String emailDomain = email.substring(email.indexOf("@")+1);
			try {
				InetAddress.getByName(emailDomain);
			}
			catch (UnknownHostException uhe) {
				errorMessage.append(" Couldn't find the domain:"+emailDomain+" Please enter a valid e-mail address.");
			}
		}
		return errorMessage.toString();
	}

	/**
	 * ALSO check the plainPassword
	 * @param plainPassword the plaintext password to check for acceptability
	 * @maram checkEmailDomain	if true, the domain part of the e-mail address will be checked in DNS to be sure it exists
	 * @return an empty string if there were no problems, or a text message listing one or more problems
	 */
	String complain(String plainPassword) {
		if (plainPassword.length() < MIN_PASSWORD_LENGTH)
			return(" Password should be at least "+MIN_PASSWORD_LENGTH+" characters long. ");
		else
			return "";
	}

	
	/**
	 * ALSO check the plainPassword
	 * @param plainPassword the plaintext password to check for acceptability
	 * @maram checkEmailDomain	if true, the domain part of the e-mail address will be checked in DNS to be sure it exists
	 * @return an empty string if there were no problems, or a text message listing one or more problems
	 */
	String complain(String plainPassword, boolean checkEmailDomain) {
		StringBuffer errorMessage = new StringBuffer();
		errorMessage.append(complain(plainPassword));
		errorMessage.append(complain(checkEmailDomain));
		return errorMessage.toString();
	}


	/**
	 * Sets an integer authentication key on this credential, having max length of maxLength
	 * @param maxLength the max length the key should be (the key may be from 1 to maxLength characters)
	 */
	void setAuthenticationKey() {
		authenticationKey = WatchYou.getNextRandomInt();
	}

	int getAuthenticationKey() {
		return authenticationKey;
	}

	boolean validateAuthenticationKey(int assertedKey) {
		return assertedKey == authenticationKey;
	}

	void touchAuthkeyLastSent() {
		authkeyLastSent = System.currentTimeMillis();
	}

	long getAuthkeyLastSent() {
		return authkeyLastSent;
	}

}

