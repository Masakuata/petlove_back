package xatal.petlove.util;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import xatal.petlove.structures.Attachment;
import xatal.petlove.structures.MIMEType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XEmail {
	private final Mailer mailer;
	private Map<String, String> recipients = new HashMap<>();

	private String subject;

	private String from;

	private String message;

	private final List<Attachment> attachments = new LinkedList<>();

	public XEmail() {
//        this.mailer = MailerBuilder
//                .withSMTPServer(
//                        "smtp.gmail.com",
//                        587,
//                        "edsonmanuelcarballovera@gmail.com",
//                        "ekqu dpsw zsuj kpds"
//                )
//                .withTransportStrategy(TransportStrategy.SMTP_TLS)
//                .buildMailer();
		this.mailer = MailerBuilder
			.withSMTPServer(
				"smtp.gmail.com",
				587,
				System.getenv("EMAIL"),
				System.getenv("EMAIL_PASSWORD")
			)
			.withTransportStrategy(TransportStrategy.SMTP_TLS)
			.buildMailer();
	}

	public void addRecipient(String name, String email) {
		if (!this.recipients.containsKey(name)) {
			this.recipients.put(name, email);
		}
	}

	public void removeRecipient(String name) {
		this.recipients.remove(name);
	}

	public void resetRecipients() {
		this.recipients = new HashMap<>();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void addAttachment(String filename, byte[] bytes, MIMEType mimeType) {
		this.attachments.add(new Attachment(filename, bytes, mimeType));
	}

	public Email makeEmail() {
		EmailPopulatingBuilder builder = EmailBuilder
			.startingBlank()
			.from(this.from)
			.withSubject(this.subject)
			.withPlainText(this.message);
		this.recipients.forEach(builder::to);
		this.attachments.forEach(attachment ->
			builder.withAttachment(
				attachment.getFilename(),
				attachment.getBytes(),
				attachment.getMimeType().toString()
			)
		);
		return builder.buildEmail();
	}

	public void send() {
		this.mailer.sendMail(this.makeEmail());
	}
}
