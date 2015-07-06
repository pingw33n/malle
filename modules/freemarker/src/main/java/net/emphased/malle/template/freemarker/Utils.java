package net.emphased.malle.template.freemarker;

import freemarker.template.TemplateException;
import net.emphased.malle.MailException;
import net.emphased.malle.template.GenericMailTemplateException;

final class Utils {

    public static MailException wrapException(TemplateException e) {
        return new GenericMailTemplateException(e);
    }

    private Utils() {
    }
}
