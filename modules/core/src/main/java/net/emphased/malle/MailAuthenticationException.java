package net.emphased.malle;

public class MailAuthenticationException extends MailException {

    private static final long serialVersionUID = -8484770398886608947L;

    public MailAuthenticationException(String message) {
        super(message);
    }

    public MailAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailAuthenticationException(Throwable cause) {
        super(cause);
    }
}
