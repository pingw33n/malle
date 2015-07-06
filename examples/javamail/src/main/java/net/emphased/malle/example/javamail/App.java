package net.emphased.malle.example.javamail;

import net.emphased.malle.*;
import net.emphased.malle.javamail.Javamail;

public class App {

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
                mailMessage.plain(html);
            }
            mailMessage.send();
        } catch (MailAddressException e) {
            err("Invalid address", e.getMessage());
        } catch (MailSendException e) {
            err("Couldn't sent", e.getMessage());
        } catch (MailAuthenticationException e) {
            err("Couldn't authenticate", e.getMessage());
        } catch (MailException e) {
            err("Unexpected mail failure", e.getMessage());
        }

        System.out.println("Success!");
    }

    private static void err(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static void err(String msg, String details) {
        if (details != null) {
            msg += " (" + details + ")";
        }
        err(msg);
    }

    private static <T> T checkNotNull(T ref, String msg) {
        checkTrue(ref != null, msg);
        return ref;
    }

    private static void checkTrue(boolean condition, String msg) {
        if (!condition) {
            err(msg);
        }
    }
}
