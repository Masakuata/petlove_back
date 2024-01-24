package xatal.petlove.reports;

import xatal.petlove.structures.Attachment;
import xatal.petlove.util.XEmail;

import java.util.LinkedList;
import java.util.List;

public abstract class XReport {
    static final String SENDER_EMAIL = System.getenv("EMAIL");

    void sendEmailWithAttachment(
            final String title,
            final String message,
            final String recipientName,
            final String recipientEmail,
            final Attachment attachment
    ) {
        List<Attachment> attachments = new LinkedList<>();
        attachments.add(attachment);

        this.sendEmailWithAttachment(
                title,
                message,
                recipientName,
                recipientEmail,
                attachments
        );
    }

    void sendEmailWithAttachment(
            final String title,
            final String message,
            final String recipientName,
            final String recipientEmail,
            final List<Attachment> attachments
    ) {
        XEmail email = new XEmail();
        email.setFrom(XReport.SENDER_EMAIL);
        email.setSubject(title);
        email.addRecipient(recipientName, recipientEmail);
        email.setMessage(message);
        attachments.forEach(attachment ->
                email.addAttachment(attachment.getFilename(), attachment.getBytes(), attachment.getMimeType()));
        email.send();
    }
}
