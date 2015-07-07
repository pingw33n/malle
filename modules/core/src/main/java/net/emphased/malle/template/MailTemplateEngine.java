package net.emphased.malle.template;

import javax.annotation.Nullable;
import java.io.Reader;
import java.util.Locale;

public interface MailTemplateEngine {

    MailTemplate getTemplate(Reader source, String name);
    MailTemplate getTemplate(String name, @Nullable Locale locale);
    MailTemplate getTemplate(String name);
}
