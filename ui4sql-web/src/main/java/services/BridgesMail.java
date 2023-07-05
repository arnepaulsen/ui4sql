/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;
/**
 *
 *Program Name
 * BridgesMail.java
 *
 *Last date of modification
 *18 Sep 2003
 *
 *Description
 * Send email via Lotus Notes
 * 
 * 
 * Usage:
 * 
 * 	mailer = new BridgesMail();
 *	mailer.sendMessage(smtpHost, sentTo[], from, subject, text );
 * 
 * Change Log:
 * 
 * 5/5/2007 - Remove hard-coded user-ids from send-to and send-from arguments.
 *   
 * 
 */

import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.*;

/**
 * @author PAULSEAR
 * 
 */

public class BridgesMail {

	public BridgesMail() {
		super();
	}

	/*
	 * Method for testing - normally should use sendMessage(host, to[], from ,
	 * subject, text)
	 */

	public void testSend(String argSendFrom, String argSendTo) {

		String mailHost = "mailhub.arnepaulsenjr.com";
		String[] sendTo = new String[] { argSendTo};
		String sendFrom = argSendFrom;
		String sendSubject = "Default subject - overridein sendMessage method.";
		String sendText = "Default text - override in sendMessage method.";

		sendMessage(mailHost, sendTo, sendFrom, sendSubject, sendText);
	}

	/**
	 * send a message (host, to[], from, subject, text) ;
	 * 
	 */

	public String sendMessage(String mailHost, String[] sendTo, String sendFrom,
			String sendSubject, String sendText) {

		String rc = new String ("ok");		

		Properties mailProps = System.getProperties();
		mailProps.put("mail.smtp.host", mailHost);

	
		/*
		 * set up message
		 */
		
	
		try {
			
			javax.mail.Session session = javax.mail.Session.getDefaultInstance(
					mailProps, null);

			javax.mail.Message msg = new MimeMessage(session);

			
			msg.setFrom(new javax.mail.internet.InternetAddress(sendFrom));

			javax.mail.internet.InternetAddress[] addressList = new javax.mail.internet.InternetAddress[sendTo.length];

			for (int i = 0; i < sendTo.length; i++) {
				addressList[i] = new javax.mail.internet.InternetAddress(
						sendTo[i]);
			}
			msg.setRecipients(javax.mail.Message.RecipientType.TO, addressList);
			msg.setSubject(sendSubject);
			msg.setSentDate(new Date());

			// Create the body text
			Multipart parts = new MimeMultipart();
			MimeBodyPart mainBody = new MimeBodyPart();
			mainBody.setText(sendText);
			parts.addBodyPart(mainBody);

			// Set some header fields
			msg.setHeader("X-Priority", "High");
			msg.setHeader("Sensitivity", "Company-Confidential");
			msg.setContent(parts);

			Transport.send(msg);

		} catch (MessagingException e) {
			rc = e.toString();
			System.out.println("Mailing Error : " + e.toString());

		}
		
		catch (Exception e) {
			rc = e.toString();
		}

		return rc;
	}

	/**
ds	 * Method main().
	 * 
	 */
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		System.out.println("Mailer Version 1.1 Starting at: " + startTime);

		if (args.length < 2 ) {
			System.out.println("Need to specify send-to and send-from as arguments.");
			System.exit(99);
			
		}
		
		BridgesMail mailer = new BridgesMail();
		mailer.testSend(args[0], args[1]);

		long endTime = System.currentTimeMillis();
		System.out.println("End time: " + endTime);
		long totalTime = endTime - startTime;
		double seconds = totalTime / 1000;

		System.out.println("Elapsed time (seconds): " + seconds);
	}
}
