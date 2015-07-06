package net.emphased.malle;

public class GenericMailException extends MailException {

    private static final long serialVersionUID = 2178265956103239915L;

    public GenericMailException(String message) {
        super(message);
    }

    public GenericMailException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericMailException(Throwable cause) {
        super(cause);
    }
}
