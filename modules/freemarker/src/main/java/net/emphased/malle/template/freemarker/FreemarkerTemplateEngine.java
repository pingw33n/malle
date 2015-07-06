package net.emphased.malle.template.freemarker;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import net.emphased.malle.MailIOException;
import net.emphased.malle.MailMessage;
import net.emphased.malle.template.MailTemplate;
import net.emphased.malle.template.MailTemplateEngine;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.emphased.malle.util.Preconditions.checkArgument;
import static net.emphased.malle.util.Preconditions.checkNotNull;

public class FreemarkerTemplateEngine implements MailTemplateEngine {

    static final String MESSAGE_VAR = "__mulle_message";

    private Configuration configuration;

    @Override
    public MailTemplate getTemplate(Reader source, String name) {
        try {
            return new FreemarkerTemplate(this, new Template(name, source, configuration));
        } catch (IOException e) {
            throw new MailIOException(e);
        }
    }

    @Override
    public MailTemplate getTemplate(String name, @Nullable Object[] discriminator) {
        checkArgument(discriminator == null ||
                discriminator.length == 1 && discriminator[0] instanceof Locale);
        Locale locale = discriminator != null ? (Locale)  discriminator[0] : null;
        try {
            return createFreemarkerTemplate(configuration.getTemplate(name, locale));
        } catch (IOException e) {
            throw new MailIOException(e);
        }
    }

    @Override
    public MailTemplate getTemplate(String name) {
        return getTemplate(name, null);
    }

    void applyTemplate(FreemarkerTemplate template, MailMessage message, Map<String, ?> context) {
        checkNotNull(template, "The 'template' must not be null");
        checkNotNull(message, "The 'message' must not be null");
        checkNotNull(context, "The 'context' must not be null");
        Map<String, Object> model = new HashMap<String, Object>(context);
        model.put(MESSAGE_VAR, new ObjectModel(message));
        try {
            Environment env = template.getTemplate().createProcessingEnvironment(model, NoopWriter.INSTANCE, null);
            configureEnvironment(env, template, message, context);
            env.process();
        } catch (TemplateException e) {
            throw Utils.wrapException(e);
        } catch (IOException e) {
            throw new MailIOException(e);
        }
    }

    public void setConfiguration(Configuration configuration) {
        checkNotNull(configuration, "The 'configuration' must not be null");
        this.configuration = (Configuration) configuration.clone();
        configure();
    }
    public FreemarkerTemplateEngine withConfiguration(Configuration configuration) {
        setConfiguration(configuration);
        return this;
    }

    private void configure() {
        Map<String, Object> vars = new HashMap<String, Object>();

        vars.put(MailDirective.NAME, new MailDirective());

        try {
            configuration.setSharedVaribles(vars);
        } catch (TemplateModelException e) {
            throw Utils.wrapException(e);
        }
    }

    private FreemarkerTemplate createFreemarkerTemplate(Template template) {
        return new FreemarkerTemplate(this, template);
    }

    private void configureEnvironment(Environment env, FreemarkerTemplate template,
                                      MailMessage message, Map<String, ?> context) {
        // Nothing for now.
    }
}
