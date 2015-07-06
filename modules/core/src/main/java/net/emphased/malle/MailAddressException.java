package net.emphased.malle;

public class MailAddressException extends MailException {

    private static final long serialVersionUID = -4132284098927564108L;

    public MailAddressException(String message) {
        super(message);
    }

    public MailAddressException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailAddressException(Throwable cause) {
        super(cause);
    }
}
