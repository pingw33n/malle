package net.emphased.malle;

public interface MailSystem {

    Mail createMail();
    Mail createMail(boolean multipart);
    void send(Mail... mail);
    void send(Iterable<? extends Mail> mail);
}
