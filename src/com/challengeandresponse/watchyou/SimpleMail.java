package com.challengeandresponse.watchyou;

import java.io.IOException;
import java.io.PrintStream;

import sun.net.smtp.SmtpClient;

public class SimpleMail {

	private String smtpServer;
	private String emailFromName;
	private String emailFromAccount;


	public SimpleMail(String smtpServer, String emailFromName, String emailFromAccount) {
		this.smtpServer = smtpServer;
		this.emailFromName = emailFromName;
		this.emailFromAccount = emailFromAccount;
	}


	public void sendMail(String to, String subject, String message)
	throws IOException {
		SmtpClient smtp = new SmtpClient(smtpServer);
		smtp.from(emailFromAccount);
		smtp.to(to);

		PrintStream msg = smtp.startMessage();
		msg.println("To: " + to);  // so mailers will display the To: address
		msg.println("From: " + emailFromName+" <"+emailFromAccount+">");
		msg.println("Subject: "+ subject);
		msg.println();
		msg.println(message);
		msg.println();
		msg.println("---");
		smtp.closeServer();
	}

}
