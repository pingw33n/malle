package net.emphased.malle.template;

public class GenericMailTemplateException extends MailTemplateException {

    private static final long serialVersionUID = 5678049360770045586L;

    public GenericMailTemplateException(String message) {
        super(message);
    }

    public GenericMailTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericMailTemplateException(Throwable cause) {
        super(cause);
    }
}
