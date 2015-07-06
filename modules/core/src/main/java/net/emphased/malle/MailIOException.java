package net.emphased.malle;

public class MailIOException extends MailException {

    private static final long serialVersionUID = 1049285101095153676L;

    public MailIOException(String message) {
        super(message);
    }

    public MailIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailIOException(Throwable cause) {
        super(cause);
    }
}
