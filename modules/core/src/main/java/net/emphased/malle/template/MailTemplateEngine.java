package net.emphased.malle.template;

import javax.annotation.Nullable;
import java.io.Reader;

public interface MailTemplateEngine {

    MailTemplate getTemplate(Reader source, String name);
    MailTemplate getTemplate(String name, @Nullable Object[] discriminator);
    MailTemplate getTemplate(String name);
}
