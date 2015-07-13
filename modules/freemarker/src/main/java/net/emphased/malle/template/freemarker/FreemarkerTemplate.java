package net.emphased.malle.template.freemarker;

import freemarker.template.Template;
import net.emphased.malle.Mail;
import net.emphased.malle.template.MailTemplate;

import javax.annotation.Nullable;
import java.util.Map;

public class FreemarkerTemplate implements MailTemplate {

    private final FreemarkerTemplateEngine engine;
    private final Template template;

    FreemarkerTemplate(FreemarkerTemplateEngine engine, Template template) {
        this.engine = engine;
        this.template = template;
    }

    public FreemarkerTemplateEngine getEngine() {
        return engine;
    }

    public Template getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return template.getName();
    }

    @Override
    public void apply(Mail message, @Nullable Map<String, ?> context) {
        engine.applyTemplate(this, message, context);
    }
}
