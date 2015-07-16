package net.emphased.malle.javamail;

import net.emphased.malle.*;
import net.emphased.malle.util.SimpleFormat;

import javax.activation.DataHandler;
import javax.annotation.Nullable;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static net.emphased.malle.util.Preconditions.checkArgument;
import static net.emphased.malle.util.Preconditions.checkNotNull;

class JavamailMessage implements Mail {

    private final Javamail javamail;
    private final MimeMessage mimeMessage = new MimeMessage((Session) null);
    private boolean mimeMessageReady;
    private MimeMultipart attachmentPart;
    private MimeMultipart inlinePart;
    private Charset charset = DEFAULT_CHARSET;
    private final Map<BodyType, Body> body = new EnumMap<>(BodyType.class);
    private Encoding bodyEncoding = DEFAULT_BODY_ENCODING;
    private Encoding attachmentEncoding = DEFAULT_ATTACHMENT_ENCODING;
    private final Map<AddressType, List<InternetAddress>> addresses = new EnumMap<>(AddressType.class);
    private final List<BodyPart> inlines = new ArrayList<>();

    private static final Map<Encoding, String> ENCODING_TO_RFC;
    static {
        EnumMap<Encoding, String> m = new EnumMap<>(Encoding.class);
        m.put(Encoding.BASE64, "base64");
        m.put(Encoding.QUOTED_PRINTABLE, "quoted-printable");
        m.put(Encoding.EIGHT_BIT, "8bit");
        m.put(Encoding.SEVEN_BIT, "7bit");
        m.put(Encoding.BINARY, "binary");
        ENCODING_TO_RFC = Collections.unmodifiableMap(m);
        if (Encoding.values().length != ENCODING_TO_RFC.size()) {
            throw new AssertionError("Not all Encoding values have mappings in ENCODING_TO_RFC");
        }
    }

    JavamailMessage(Javamail javamail, MultipartMode multipartMode) {
        checkNotNull(javamail, "The 'javamail' can't be null");
        checkNotNull(multipartMode, "The 'multipartMode' can't be null");
        this.javamail = javamail;
        try {
            createMimeMultiparts(multipartMode);
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
    }

    public MimeMessage getMimeMessage() {
        ensureMimeMessageReady();
        return mimeMessage;
    }

    @Override
    public Mail charset(Charset charset) {
        checkNotNull(charset, "The 'charset' can't be null");
        this.charset = charset;
        return this;
    }

    @Override
    public Mail charset(String charset) {
        charset(Charset.forName(charset));
        return this;
    }

    @Override
    public Mail bodyEncoding(@Nullable Encoding encoding) {
        bodyEncoding = encoding;
        return this;
    }

    @Override
    public Mail attachmentEncoding(@Nullable Encoding encoding) {
        this.attachmentEncoding = encoding;
        return this;
    }

    @Override
    public Mail id(String id) {
        checkNotNull(id, "The 'id' can't be null");
        try {
            mimeMessage.setHeader("Message-ID", '<' + id + '>');
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
        return this;
    }

    @Override
    public Mail priority(int priority) {
        try {
            mimeMessage.setHeader("X-Priority", String.valueOf(priority));
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
        return this;
    }

    @Override
    public Mail from(Iterable<String> addresses) {
        return address(AddressType.FROM, addresses);
    }

    @Override
    public Mail from(String[] addresses) {
        return address(AddressType.FROM, addresses);
    }

    @Override
    public Mail from(String addresses) {
        return address(AddressType.FROM, addresses);
    }

    @Override
    public Mail from(String address, @Nullable String personal) {
        return address(AddressType.FROM, address, personal);
    }

    @Override
    public Mail replyTo(Iterable<String> addresses) {
        return address(AddressType.REPLY_TO, addresses);
    }

    @Override
    public Mail replyTo(String[] addresses) {
        return address(AddressType.REPLY_TO, addresses);
    }

    @Override
    public Mail replyTo(String addresses) {
        return address(AddressType.REPLY_TO, addresses);
    }

    @Override
    public Mail replyTo(String address, @Nullable String personal) {
        return address(AddressType.REPLY_TO, address, personal);
    }

    @Override
    public Mail to(Iterable<String> addresses) {
        return address(AddressType.TO, addresses);
    }

    @Override
    public Mail to(String[] addresses) {
        return address(AddressType.TO, addresses);
    }

    @Override
    public Mail to(String addresses) {
        return address(AddressType.TO, addresses);
    }

    @Override
    public Mail to(String address, @Nullable String personal) {
        return address(AddressType.TO, createAddress(address, personal));
    }

    @Override
    public Mail cc(Iterable<String> addresses) {
        return address(AddressType.CC, addresses);
    }

    @Override
    public Mail cc(String[] addresses) {
        return address(AddressType.CC, addresses);
    }

    @Override
    public Mail cc(String addresses) {
        return address(AddressType.CC, addresses);
    }

    @Override
    public Mail cc(String address, @Nullable String personal) {
        return address(AddressType.CC, createAddress(address, personal));
    }

    @Override
    public Mail bcc(Iterable<String> addresses) {
        return address(AddressType.BCC, addresses);
    }

    public Mail bcc(String addresses) {
        return address(AddressType.BCC, addresses);
    }

    @Override
    public Mail bcc(String[] addresses) {
        return address(AddressType.BCC, addresses);
    }

    @Override
    public Mail bcc(String address, @Nullable String personal) {
        return address(AddressType.BCC, createAddress(address, personal));
    }

    @Override
    public Mail address(AddressType type, Iterable<String> addresses) {
        for (String a: addresses) {
            address(type, a);
        }
        return this;
    }

    @Override
    public Mail address(AddressType type, String[] addresses) {
        return address(type, Utils.toIterable(addresses));
    }

    @Override
    public Mail address(AddressType type, String addresses) {
        for(InternetAddress ia: parseAddresses(addresses)) {
            address(type, ia);
        }
        return this;
    }

    @Override
    public Mail address(AddressType type, String address, @Nullable String personal) {
        return address(type, createAddress(address, personal));
    }

    @Override
    public Mail subject(String subject) {
        checkNotNull(subject, "The 'subject' must not be null");
        try {
            mimeMessage.setSubject(subject, charset.name());
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
        return this;
    }

    @Override
    public Mail header(String name, String value) {
        checkNotNull(name, "The 'name' must not be null");
        checkNotNull(value, "The 'value' must not be null");
        try {
            mimeMessage.addHeader(name, MimeUtility.fold(9, MimeUtility.encodeText(value, charset.name(), null)));
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Shouldn't happen", e);
        }
        return this;
    }

    @Override
    public Mail header(String name, String pattern, Object... words) {
        checkNotNull(name, "The 'name' must not be null");
        checkNotNull(pattern, "The 'pattern' must not be null");
        try {
            if (words.length != 0) {
                for (int i = 0; i < words.length; i++) {
                    Object arg = words[i];
                    words[i] = arg != null ? MimeUtility.encodeText(arg.toString(), charset.name(), null) : null;
                }
                pattern = SimpleFormat.format(pattern, words);
            }
            mimeMessage.addHeader(name, MimeUtility.fold(9, pattern));
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Shouldn't happen", e);
        }
        return this;
    }

    @Override
    public Mail plain(String plain) {
        checkNotNull(plain, "The 'plain' must not be null");
        return body(BodyType.PLAIN, plain);
    }

    @Override
    public Mail html(String html) {
        checkNotNull(html, "The 'html' must not be null");
        return body(BodyType.HTML, html);
    }

    @Override
    public Mail body(BodyType type, String value) {
        checkNotNull(type, "The 'type' must not be null");
        checkNotNull(value, "The 'value' must not be null");
        body.put(type, new Body(value));
        mimeMessageReady = false;
        return this;
    }

    @Override
    public Mail attachment(InputStreamSupplier content, String filename, String type) {
        checkNotNull(content, "The 'content' can't be null");
        checkNotNull(filename, "The 'filename' can't be null");
        checkNotNull(type, "The 'type' can't be null");
        try {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
            try {
                mimeBodyPart.setFileName(MimeUtility.encodeWord(filename, charset.name(), null));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Shouldn't happen", ex);
            }
            mimeBodyPart.setDataHandler(new DataHandler(new InputStreamSupplierDatasource(content, type, filename)));
            setContentTransferEncodingHeader(mimeBodyPart, attachmentEncoding);
            getAttachmentPart().addBodyPart(mimeBodyPart);
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
        return this;
    }

    @Override
    public Mail attachment(InputStreamSupplier content, String filename) {
        return attachment(content, filename, "application/octet-stream");
    }

    @Override
    public Mail inline(InputStreamSupplier content, String id, String type) {
        checkNotNull(content, "The 'content' can't be null");
        checkNotNull(id, "The 'id' can't be null");
        checkNotNull(type, "The 'type' can't be null");
        checkMultipart();
        try {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
            mimeBodyPart.setContentID('<' + id + '>');
            mimeBodyPart.setDataHandler(new DataHandler(new InputStreamSupplierDatasource(content, type, "inline")));
            setContentTransferEncodingHeader(mimeBodyPart, attachmentEncoding);
            inlines.add(mimeBodyPart);
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
        return this;
    }

    @Override
    public Mail inline(InputStreamSupplier content, String id) {
        return inline(content, id, "application/octet-stream");
    }

    @Override
    public Mail template(String name, @Nullable Locale locale, Map<String, ?> context) {
        javamail.applyTemplate(this, name, locale, context);
        return this;
    }

    @Override
    public Mail template(String name, Map<String, ?> context) {
        return template(name, null, context);
    }

    @Override
    public Mail template(String name, @Nullable Locale locale, Object... context) {
        checkArgument(context.length % 2 == 0, "The 'context' varargs must contain an even number of values");
        Map<String, Object> contextMap;
        if (context.length != 0) {
            int len = context.length / 2;
            contextMap = new HashMap<>(len);
            for (int i = 0; i < len; i++) {
                Object key = context[i * 2];
                if (!(key instanceof String)) {
                    checkArgument(false, "The keys in 'context' must be of String type");
                }
                Object value = context[i * 2 + 1];
                contextMap.put((String) key, value);
            }
        } else {
            contextMap = null;
        }
        return template(name, locale, contextMap);
    }

    @Override
    public Mail template(String name, Object... context) {
        return template(name, null, context);
    }

    @Override
    public Mail send() {
        javamail.send(this);
        return this;
    }

    @Override
    public Mail writeTo(OutputStream outputStream) {
        try {
            getMimeMessage().writeTo(outputStream);
        } catch (IOException e) {
            throw new MailIOException(e);
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }
        return this;
    }

    @Override
    public Mail writeTo(File file) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            return writeTo(os);
        } catch (IOException e) {
            throw new MailIOException(e);
        }
    }

    private Mail address(AddressType type, InternetAddress address) {
        List<InternetAddress> l = addresses.get(type);
        if (l == null) {
            l = new ArrayList<>();
            addresses.put(type, l);
        }
        l.add(address);
        mimeMessageReady = false;
        return this;
    }

    private InternetAddress[] parseAddresses(String addresses) {
        try {
            InternetAddress[] r = InternetAddress.parse(addresses, false);
            for (int i = 0; i < r.length; i++) {
                InternetAddress ia = r[i];
                String personal = ia.getPersonal();
                // Force recoding of the personal part. This will also encode a possibly
                // unencoded personal that needs encoding because of the non US-ASCII characters.
                r[i] = new InternetAddress(ia.getAddress(), personal, charset.name());
            }
            return r;
        } catch (AddressException e) {
            throw Utils.wrapException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Shouldn't happen", e);
        }
    }

    private InternetAddress createAddress(String address, @Nullable String personal) {
        checkNotNull(address, "The 'address' can't be null");
        InternetAddress r;
        try {
            r = new InternetAddress(address, personal, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Shouldn't happen", e);
        }
        try {
            r.validate();
        } catch (AddressException e) {
            throw Utils.wrapException(e);
        }
        return r;
    }

    private void createMimeMultiparts(MultipartMode multipartMode) throws MessagingException {
        switch (multipartMode) {
            case NONE:
                attachmentPart = null;
                inlinePart = null;
                break;
            case MIXED:
                attachmentPart = new MimeMultipart("mixed");
                mimeMessage.setContent(attachmentPart);
                inlinePart = attachmentPart;
                break;
            case RELATED:
                attachmentPart = new MimeMultipart("related");
                mimeMessage.setContent(attachmentPart);
                inlinePart = attachmentPart;
                break;
            case MIXED_RELATED:
                attachmentPart = new MimeMultipart("mixed");
                mimeMessage.setContent(attachmentPart);
                inlinePart = new MimeMultipart("related");
                MimeBodyPart relatedBodyPart = new MimeBodyPart();
                relatedBodyPart.setContent(inlinePart);
                attachmentPart.addBodyPart(relatedBodyPart);
                break;
            default:
                throw new AssertionError();
        }
    }

    private MimeBodyPart getTextPart() throws MessagingException {
        checkMultipart();
        MimeBodyPart bodyPart = null;
        for (int i = 0; i < inlinePart.getCount(); i++) {
            BodyPart bp = inlinePart.getBodyPart(i);
            if (bp.getFileName() == null) {
                bodyPart = (MimeBodyPart) bp;
            }
        }
        if (bodyPart == null) {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            inlinePart.addBodyPart(mimeBodyPart);
            bodyPart = mimeBodyPart;
        }
        return bodyPart;
    }

    private void setText(Body plain, Body html) throws MessagingException {
        MimeMultipart messageBody = new MimeMultipart("alternative");
        getTextPart().setContent(messageBody, "text/alternative");

        // Create the plain text part of the message.
        MimeBodyPart plainTextPart = new MimeBodyPart();
        setPlainTextToMimePart(plainTextPart, plain);
        messageBody.addBodyPart(plainTextPart);

        // Create the HTML text part of the message.
        MimeBodyPart htmlTextPart = new MimeBodyPart();
        setHtmlToMimePart(htmlTextPart, html);
        messageBody.addBodyPart(htmlTextPart);
    }

    private void setText(Body text, boolean html) throws MessagingException {
        MimePart partToUse;
        if (isMultipart()) {
            partToUse = getTextPart();
        }
        else {
            partToUse = this.mimeMessage;
        }
        if (html) {
            setHtmlToMimePart(partToUse, text);
        }
        else {
            setPlainTextToMimePart(partToUse, text);
        }
    }

    private void setTextToMimePart(MimePart mimePart, String text, String type, @Nullable Encoding encoding) throws MessagingException {
        mimePart.setContent(text, type + "; charset=" + charset.name());
        setContentTransferEncodingHeader(mimePart, encoding);
    }

    private void setContentTransferEncodingHeader(Part part, @Nullable Encoding encoding) throws MessagingException {
        if (encoding != null) {
            part.setHeader("Content-Transfer-Encoding", checkNotNull(ENCODING_TO_RFC.get(encoding)));
        } else {
            part.removeHeader("Content-Transfer-Encoding");
        }
    }

    private void setPlainTextToMimePart(MimePart part, Body text) throws MessagingException {
        setTextToMimePart(part, text.text, "text/plain", text.bodyEncoding);
    }

    private void setHtmlToMimePart(MimePart part, Body text) throws MessagingException {
        setTextToMimePart(part, text.text, "text/html", text.bodyEncoding);
    }

    private boolean isMultipart() {
        return attachmentPart != null;
    }

    private void checkMultipart() {
        if (!isMultipart()) {
            throw new IllegalStateException("This message is not multipart");
        }
    }

    private MimeMultipart getAttachmentPart() {
        checkMultipart();
        return attachmentPart;
    }

    private MimeMultipart getInlinePart() throws IllegalStateException {
        checkMultipart();
        return inlinePart;
    }

    private void ensureMimeMessageReady() {
        if (mimeMessageReady) {
            return;
        }
        try {
            for (Map.Entry<AddressType, List<InternetAddress>> entry: addresses.entrySet()) {
                List<InternetAddress> l = entry.getValue();
                switch (entry.getKey()) {
                    case FROM:
                        mimeMessage.removeHeader("From");
                        if (!l.isEmpty()) {
                            mimeMessage.addFrom(Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case REPLY_TO:
                        if (!l.isEmpty()) {
                            mimeMessage.setReplyTo(Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case TO:
                        mimeMessage.removeHeader("To");
                        if (!l.isEmpty()) {
                            mimeMessage.addRecipients(Message.RecipientType.TO, Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case CC:
                        mimeMessage.removeHeader("CC");
                        if (!l.isEmpty()) {
                            mimeMessage.addRecipients(Message.RecipientType.CC, Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case BCC:
                        mimeMessage.removeHeader("BCC");
                        if (!l.isEmpty()) {
                            mimeMessage.addRecipients(Message.RecipientType.BCC, Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    default:
                        throw new AssertionError("Unhandled RecipientType: " + entry.getKey());
                }
            }

            Body plain = body.get(BodyType.PLAIN);
            Body html = body.get(BodyType.HTML);
            if (plain != null && html != null) {
                checkMultipart();
                setText(plain, html);
            } else if (plain != null) {
                setText(plain, false);
            } else if (html != null) {
                setText(html, true);
            } else {
                throw new IllegalStateException("The message must have plain and/or html text set");
            }

            if (!inlines.isEmpty()) {
                MimeMultipart target = getInlinePart();
                for (BodyPart part: inlines) {
                    target.addBodyPart(part);
                }
            }
        } catch (MessagingException e) {
            throw Utils.wrapException(e);
        }

        mimeMessageReady = true;
    }

    private class Context {
        final Charset charset;
        final Encoding bodyEncoding;

        public Context() {
            charset = JavamailMessage.this.charset;
            bodyEncoding = JavamailMessage.this.bodyEncoding;
        }
    }

    private class Body extends Context {

        final String text;

        public Body(String text) {
            this.text = text;
        }
    }
}
