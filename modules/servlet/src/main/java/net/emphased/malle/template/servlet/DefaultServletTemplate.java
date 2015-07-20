package net.emphased.malle.template.servlet;

import net.emphased.malle.Mail;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

public class DefaultServletTemplate implements ServletTemplate {

    private final ServletTemplateEngine engine;
    private final String name;
    private final Locale locale;

    public DefaultServletTemplate(ServletTemplateEngine engine, String name, Locale locale) {
        this.engine = engine;
        this.name = name;
        this.locale = locale;
    }

    public ServletTemplateEngine getEngine() {
        return engine;
    }

    @Override
    public String getName() {
        return name;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public void apply(Mail message, @Nullable Map<String, ?> context) {
        engine.applyTemplate(this, message, context);
    }
}
