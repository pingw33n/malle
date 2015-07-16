package net.emphased.malle;

public interface MailSystem {

    Mail createMail();
    Mail createMail(boolean multipart);
}
