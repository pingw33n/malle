package net.emphased.malle.javamail;

import net.emphased.malle.Mail;
import net.emphased.malle.MailSendException;
import net.emphased.malle.MailSystem;
import net.emphased.malle.template.MailTemplate;
import net.emphased.malle.template.MailTemplateEngine;

import javax.annotation.Nullable;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static net.emphased.malle.util.Preconditions.*;

public class Javamail implements MailSystem {

    public static final String DEFAULT_PROTOCOL = "smtp";
    public static final String PASSWORD_PROP = "malle.javamail.password";

    private final Object monitor = new Object();
    private Session session;
    private Map<String, String> properties = new HashMap<>();
    private MailTemplateEngine templateEngine;

    @Override
    public Mail mail() {
        return mail(true);
    }

    @Override
    public Mail mail(boolean multipart) {
        return createMail(multipart ? MultipartMode.MIXED_RELATED : MultipartMode.NONE);
    }

    @Override
    public void send(Mail... mail) {
        send(Utils.toIterable(mail));
    }

    @Override
    public void send(Iterable<? extends Mail> mail) {
        for (Mail m: mail) {
            checkArgument(m instanceof JavamailMessage,
                    "Expected instance of JavamailMessage but found: %s", m != null ? m.getClass() : null);
        }

        @SuppressWarnings("unchecked")
        Iterable<JavamailMessage> messages = (Iterable<JavamailMessage>) mail;
        doSend(messages);
    }

    void applyTemplate(JavamailMessage message, String name, @Nullable Locale locale, Map<String, ?> context) {
        checkState(templateEngine != null, "Please the the template engine first");
        checkNotNull(name, "The 'name' can't be null");
        MailTemplate t = templateEngine.getTemplate(name, locale);
        applyTemplate(message, t, context);
    }

    void applyTemplate(JavamailMessage message, MailTemplate template, Map<String, ?> context) {
        template.apply(message, context);
    }

    private Mail createMail(MultipartMode multipartMode) {
        return new JavamailMessage(this, multipartMode);
    }

    private void doSend(Iterable<JavamailMessage> messages) {
        try {
            Session s = getOrCreateSession();
            Transport t = s.getTransport(getProtocol(s));
            boolean connected = false;
            try {
                for (JavamailMessage jmsg: messages) {
                    MimeMessage m = jmsg.getMimeMessage();

                    Address[] to = m.getRecipients(Message.RecipientType.TO);
                    if (to == null) {
                        to = new Address[0];
                    }
                    Address[] ccAndBcc = concat(m.getRecipients(Message.RecipientType.CC),
                            m.getRecipients(Message.RecipientType.BCC));
                    if (to.length + ccAndBcc.length == 0) {
                        throw new MailSendException("No mail recipients specified");
                    }

                    if (!connected) {
                        t.connect(null, -1, null, getPassword(s));
                        connected = true;
                    }

                    if (jmsg.isPersonalize() && to.length > 1) {
                        Address[] addrs = concat(new Address[1], ccAndBcc);
                        for (Address addr: to) {
                            m.setRecipient(Message.RecipientType.TO, addr);
                            addrs[0] = addr;
                            doSend(t, m, addrs);
                            // Ensure we get a new Message-ID for the next iteration.
                            m.removeHeader("Message-ID");
                        }
                    } else {
                        doSend(t, m, concat(to, ccAndBcc));
                    }
                }
            } finally {
                if (connected) {
                    t.close();
                }
            }
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }

    }

    private Address[] concat(@Nullable Address[] a1, @Nullable Address[] a2) {
        int a1len = len(a1);
        int a2len = len(a2);
        Address[] r = new Address[a1len + a2len];
        if (a1len != 0) {
            System.arraycopy(a1, 0, r, 0, a1len);
        }
        if (a2len != 0) {
            System.arraycopy(a2, 0, r, a1len, a2len);
        }
        return r;
    }

    private int len(@Nullable Address[] a) {
        return a != null ? a.length : 0;
    }

    private void doSend(Transport t, MimeMessage m, Address[] addrs) throws MessagingException {
        t.sendMessage(m, addrs);
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
        this.properties = new HashMap<>(properties);
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

