package net.emphased.malle;

public abstract class MailException extends RuntimeException {

    private static final long serialVersionUID = -7157087660127633735L;

    public MailException(String message) {
        super(message);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailException(Throwable cause) {
        this(cause != null ? cause.getMessage() : null, cause);
    }
}
