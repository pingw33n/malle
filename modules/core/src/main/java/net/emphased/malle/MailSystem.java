package net.emphased.malle;

/**
 * {@link Mail} factory interface.
 */
public interface MailSystem {

    Mail mail();
    Mail mail(boolean multipart);
    void send(Mail... mail);
    void send(Iterable<? extends Mail> mail);
}
