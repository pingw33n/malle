package net.emphased.malle.javamail;

import net.emphased.malle.MailSystem;
import net.emphased.malle.template.MailTemplate;
import net.emphased.malle.template.MailTemplateEngine;

import javax.annotation.Nullable;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static net.emphased.malle.util.Preconditions.checkNotNull;
import static net.emphased.malle.util.Preconditions.checkState;

public class Javamail implements MailSystem {

    public static final String DEFAULT_PROTOCOL = "smtp";
    public static final String PASSWORD_PROP = "malle.javamail.password";

    private final Object monitor = new Object();
    private Session session;
    private Map<String, String> properties = new HashMap<String, String>();
    private MailTemplateEngine templateEngine;

    @Override
    public JavamailMessage createMail(boolean multipart) {
        return createMail(multipart ? MultipartMode.MIXED_RELATED : MultipartMode.NONE);
    }

    JavamailMessage createMail(MultipartMode multipartMode) {
        return new JavamailMessage(this, multipartMode);
    }

    void applyTemplate(JavamailMessage message, @Nullable String name, @Nullable Locale locale, Map<String, ?> context) {
        checkState(templateEngine != null, "Please the the template engine first");
        MailTemplate t = templateEngine.getTemplate(name, locale);
        applyTemplate(message, t, context);
    }

    void applyTemplate(JavamailMessage message, MailTemplate template, @Nullable Map<String, ?> context) {
        template.apply(message, context);
    }

    void send(JavamailMessage message, String... addresses) {
        MimeMessage m = message.getMimeMessage();
        try {
            Address[] addrs = addresses.length != 0 ? parseAddresses(addresses) : m.getAllRecipients();
            doSend(m, addrs);
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
    }

    private void doSend(MimeMessage m, Address[] addrs) {
        try {
            String messageId = m.getMessageID();

            m.saveChanges();

            if (messageId != null) {
                m.setHeader("Message-ID", messageId);
            }

            Session s = getOrCreateSession();
            Transport t = s.getTransport(getProtocol(s));
            try {
                t.connect(null, -1, null, getPassword(s));
                t.sendMessage(m, addrs);
            } finally {
                t.close();
            }
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
    }

    private String getPassword(Session s) {
        return s.getProperty(PASSWORD_PROP);
    }

    private String getProtocol(Session s) {
        String r = s.getProperty("mail.transport.protocol");
        return r != null ? r : DEFAULT_PROTOCOL;
    }

    private Address[] parseAddresses(String[] addrs) throws AddressException {
        Address[] r = new Address[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            r[i] = new InternetAddress(addrs[i]);
        }
        return r;
    }

    private Session getOrCreateSession() {
        synchronized (monitor) {
            if (session == null) {
                Properties props = (Properties) System.getProperties().clone();
                props.putAll(properties);
                session = Session.getInstance(props);
            }
            return session;
        }
    }

    public void setSession(@Nullable Session session) {
        this.session = session;
    }

    public Javamail withSession(@Nullable Session session) {
        setSession(session);
        return null;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new HashMap<String, String>(properties);
        setSession(null);
    }

    public Javamail withProperties(Map<String, String> properties) {
        setProperties(properties);
        return this;
    }

    public Javamail withProperty(String name, String value) {
        checkNotNull(name, "The 'name' must not be null");
        checkNotNull(value, "The 'value' must not be null");
        properties.put(name, value);
        return this;
    }

    public void setTemplateEngine(MailTemplateEngine templateEngine) {
        checkNotNull(templateEngine, "The 'templateEngine' must not be null");
        this.templateEngine = templateEngine;
    }

    public Javamail withTemplateEngine(MailTemplateEngine templateEngine) {
        setTemplateEngine(templateEngine);
        return this;
    }
}

