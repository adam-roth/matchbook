package au.com.suncoastpc.auth.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

/**
 * Utility class for sending e-mails using JavaMail; will attempt to send mail via the configured outgoing SMTP 
 * server, or if no outgoing mail server is configured will attempt a direct connection to the recipient's 
 * incoming SMTP server.
 * 
 * @author Adam
 */
public class EmailUtility {
	private static final Logger LOG = Logger.getLogger(EmailUtility.class);
	
	public static boolean sendEmail(String fromAddress, String to, String subject, String message, String fromHeaderValue, String toHeaderValue) {
		try {
			Session session = getMailSession(to);
			
			Message mailMessage = new MimeMessage(session);
			mailMessage.setFrom(new InternetAddress(fromAddress));
			if (fromHeaderValue != null) {
				mailMessage.setHeader("From", fromHeaderValue);
			}
			if (toHeaderValue != null) {
				mailMessage.setHeader("To", toHeaderValue);
			}
			mailMessage.setHeader("Date", new Date().toString());
			mailMessage.setRecipients(RecipientType.TO, InternetAddress.parse(to, false));
			mailMessage.setSubject(subject);
			mailMessage.setContent(message, "text/html;charset=UTF-8");
			
			Transport.send(mailMessage);
			
			return true;
		} catch (Throwable e) {
			LOG.error("Failed to send e-mail!", e);
			return false;
		} 
	}
	
	private static Session getMailSession(String recipient) {
		Session session = null;
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			session = (Session) envCtx.lookup("mail/Session");
		} 
		catch (Exception e) {
			LOG.warn("Could not get mail session from JNDI, will attempt to configure it manually", e);
			session = defaultMailSession(recipient);
		}
		
		LOG.info("Value of mail.smtp.host:  " + session.getProperty("mail.smtp.host"));
		if (! Configuration.getLocalMailServer() && (session.getProperty("mail.smtp.host") == null || "localhost".equals(session.getProperty("mail.smtp.host")))) {
			LOG.warn("Invalid outgoing SMTP server detected, will attempt to override...");
			session = defaultMailSession(recipient);
		}
		
		return session;
	}
	
	private static Session defaultMailSession(String recipient) {
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", mailServerForAddress(recipient));
		return Session.getInstance(properties);
	}
	
	//code below here sourced from http://www.rgagnon.com/javadetails/java-0452.html with modifications to support Java5 features
	private static String mailServerForAddress( String address ) {
		// Find the separator for the domain name
		int pos = address.indexOf('@');

		// If the address does not contain an '@', it's not valid
		if (pos == -1) {
			return null;
		}

		try {
			InternetAddress.parse(address, false);
		} catch (AddressException e) {
			LOG.info("Address failed to parse:  " + address, e);
			return null;
		}

		// Isolate the domain/machine name and get a list of mail exchangers
		String domain = address.substring(++pos);
		List<String> mxList = null;
		try {
			mxList = getMX(domain);
		} catch (NamingException ex) {
			return null;
		}

		// Just because we can send mail to the domain, doesn't mean that the
		// address is valid, but if we can't, it's a sure sign that it isn't
		if (mxList.size() == 0) {
			return null;
		}

	    // Now, do the SMTP validation, try each mail exchanger until we get
	    // a positive acceptance. It *MAY* be possible for one MX to allow
	    // a message [store and forwarder for example] and another [like
	    // the actual mail server] to reject it. This is why we REALLY ought
	    // to take the preference into account.
	     
	    //too slow to be practical; if the address parsed and we were able to find a candidate MX server for it, assume that it is valid
		/*for (int mx = 0; mx < mxList.size(); mx++) {
			boolean valid = false;
			try {
				int res;
				//
				Socket skt = new Socket((String) mxList.get(mx), 25);
				BufferedReader rdr = new BufferedReader(new InputStreamReader(
						skt.getInputStream()));
				BufferedWriter wtr = new BufferedWriter(new OutputStreamWriter(
						skt.getOutputStream()));

				res = hear(rdr);
				if (res != 220)
					throw new Exception("Invalid header");
				say(wtr, "EHLO rgagnon.com");

				res = hear(rdr);
				if (res != 250)
					throw new Exception("Not ESMTP");

				// validate the sender address
				say(wtr, "MAIL FROM: <tim@orbaker.com>");
				res = hear(rdr);
				if (res != 250)
					throw new Exception("Sender rejected");

				say(wtr, "RCPT TO: <" + address + ">");
				res = hear(rdr);

				// be polite
				say(wtr, "RSET");
				hear(rdr);
				say(wtr, "QUIT");
				hear(rdr);
				if (res != 250)
					throw new Exception("Address is not valid!");

				valid = true;
				rdr.close();
				wtr.close();
				skt.close();
			} catch (Exception ex) {
				// Do nothing but try next host
				ex.printStackTrace();
			} finally {
				if (valid)
					return mxList.get(mx);
			}
		}*/
		return mxList.get(0);
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> getMX(String hostName) throws NamingException {
		// Perform a DNS lookup for MX records in the domain
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[] { "MX" });
		Attribute attr = attrs.get("MX");

		// if we don't have an MX record, try the machine itself
		if ((attr == null) || (attr.size() == 0)) {
			attrs = ictx.getAttributes(hostName, new String[] { "A" });
			attr = attrs.get("A");
			if (attr == null) {
				throw new NamingException("No match for name '" + hostName + "'");
			}
		}
		// Huzzah! we have machines to try. Return them as an array list
		// NOTE: We SHOULD take the preference into account to be absolutely
		// correct. This is left as an exercise for anyone who cares.
		List<String> res = new ArrayList<String>();
		NamingEnumeration<String> en = (NamingEnumeration<String>) attr.getAll();

		while (en.hasMore()) {
			String mailhost;
			String next = en.next();
			String parts[] = next.split(" ");
			// THE fix *************
			if (parts.length == 1) {
				mailhost = parts[0];
			}
			else if (parts[1].endsWith(".")) {
				mailhost = parts[1].substring(0, (parts[1].length() - 1));
			}
			else {
				mailhost = parts[1];
			}
			// THE fix *************
			res.add(mailhost);
		}
		return res;
	}
}
