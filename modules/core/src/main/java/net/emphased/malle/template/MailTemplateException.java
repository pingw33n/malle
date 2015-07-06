package net.emphased.malle.template;

import net.emphased.malle.MailException;

public abstract class MailTemplateException extends MailException {

    private static final long serialVersionUID = 6801318051775018541L;

    public MailTemplateException(String message) {
        super(message);
    }

    public MailTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailTemplateException(Throwable cause) {
        super(cause);
    }
}
