package net.emphased.malle;

public class MailSendException extends MailException {

    private static final long serialVersionUID = -9096003566080238075L;

    public MailSendException(String message) {
        super(message);
    }

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailSendException(Throwable cause) {
        super(cause);
    }
}
