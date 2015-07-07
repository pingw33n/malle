package net.emphased.malle.example.javamail;

import net.emphased.malle.*;
import net.emphased.malle.example.AbstractExample;
import net.emphased.malle.javamail.Javamail;

public class JavamailExample extends AbstractExample {

    public static void main(String[] args) {
        String from = System.getProperty("from");
        if (from == null) {
            from = System.getProperty("mail.user");
        }
        if (from == null) {
            err("Please set 'from' and/or 'mail.user' property");
        }

        String to = checkNotNull(System.getProperty("to"), "Please set 'to' property");
        String subject = checkNotNull(System.getProperty("subject"), "Please set 'subject' property");

        String plain = System.getProperty("plain");
        String html = System.getProperty("html");
        if (plain == null && html == null) {
            throw new IllegalArgumentException("Please set 'plain' and/or 'html' property");
        }

        System.out.println("Sending '" + subject + "' mail from " + from + " to " + to + "...");

        try {
            MailMessage mailMessage = new Javamail()
                    .createMailMessage(true)
                    .from(from)
                    .to(to)
                    .subject(subject);
            if (plain != null) {
                mailMessage.plain(plain);
            }
            if (html != null) {
                mailMessage.html(html);
            }
            mailMessage.send();
        } catch (MailException e) {
            handleException(e);
        }

        System.out.println("Success!");
    }


}
