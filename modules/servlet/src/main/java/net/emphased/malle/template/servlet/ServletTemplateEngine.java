package net.emphased.malle.template.servlet;

import net.emphased.malle.Mail;
import net.emphased.malle.MailIOException;
import net.emphased.malle.template.GenericMailTemplateException;
import net.emphased.malle.template.MailTemplate;
import net.emphased.malle.template.MailTemplateEngine;

import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.emphased.malle.util.Preconditions.checkNotNull;

public class ServletTemplateEngine implements MailTemplateEngine {

    public static final String MAIL_ATTR = "net.emphased.malle.MAIL";

    private ServletContext servletContext;

    @Override
    public MailTemplate getTemplate(Reader source, String name) {
        throw new UnsupportedOperationException("Obtaining template from source is not supported");
    }

    @Override
    public MailTemplate getTemplate(String name, @Nullable Locale locale) {
        return new DefaultServletTemplate(this, name, locale);
    }

    @Override
    public MailTemplate getTemplate(String name) {
        return getTemplate(name, null);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = checkNotNull(servletContext);
    }

    public ServletTemplateEngine withServletContext(ServletContext servletContext) {
        setServletContext(servletContext);
        return this;
    }

    void applyTemplate(DefaultServletTemplate template, Mail mail, Map<String, ?> context) {
        checkNotNull(servletContext, "The 'servletContext' must be set first");
        checkNotNull(template, "The 'template' must not be null");
        checkNotNull(mail, "The 'mail' must not be null");

        Map<String, Object> ctx = new HashMap<>(context != null ? context : Collections.<String, Object>emptyMap());

        HttpServletRequest request = getContentProp(ctx, ServletTemplate.REQUEST, HttpServletRequest.class);

        ctx.put(MAIL_ATTR, mail);

        for (Map.Entry<String, ?> prop: ctx.entrySet()) {
            request.setAttribute(prop.getKey(), prop.getValue());
        }
        try {
            RequestDispatcher dispatcher = servletContext.getRequestDispatcher(template.getName());
            if (dispatcher == null) {
                throw new GenericMailTemplateException("Couldn't get RequestDispatcher for: " + template.getName());
            }
            try {
                dispatcher.include(request, new DummyHttpServletResponse());
            } catch (ServletException e) {
                throw new GenericMailTemplateException(e);
            } catch (IOException e) {
                throw new MailIOException(e);
            }
        } finally {
            request.removeAttribute(MAIL_ATTR);
            for (Map.Entry<String, ?> prop: ctx.entrySet()) {
                request.removeAttribute(prop.getKey());
            }
        }
    }

    private <T> T getContentProp(Map<String, ?> context, String name, Class<T> clazz) {
        Object r = context.remove(name);
        if (r == null) {
            throw new IllegalArgumentException("The context must have '" + name + "' property set");
        }
        if (!clazz.isInstance(r)) {
            throw new IllegalArgumentException("The context must have '" + name + "' property be of type " + clazz.getSimpleName());
        }
        return clazz.cast(r);
    }
}
