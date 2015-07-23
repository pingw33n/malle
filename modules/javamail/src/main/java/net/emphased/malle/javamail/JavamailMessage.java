package net.emphased.malle.javamail;

import net.emphased.malle.*;

import javax.activation.DataHandler;
import javax.annotation.Nullable;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static net.emphased.malle.util.Preconditions.checkArgument;
import static net.emphased.malle.util.Preconditions.checkNotNull;

class JavamailMessage implements Mail {

    private final MultipartMode multipartMode;

    private final Javamail javamail;
    private Charset charset = DEFAULT_CHARSET;
    private final Map<BodyType, String> bodies = new EnumMap<>(BodyType.class);
    private Encoding bodyEncoding = DEFAULT_BODY_ENCODING;
    private Encoding attachmentEncoding = DEFAULT_ATTACHMENT_ENCODING;
    private final Map<AddressType, List<InternetAddress>> addresses = new EnumMap<>(AddressType.class);
    private final Map<Attachment.Type, List<Attachment>> attachments = new EnumMap<>(Attachment.Type.class);
    private String id;
    private Integer priority;
    private String subject;
    private final Map<String, String> headers = new LinkedHashMap<>();

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
        this.multipartMode = multipartMode;
        this.javamail = javamail;
    }

    MimeMessage getMimeMessage() {
        return new MimeMessageBuilder().build();
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
    public Mail attachmentEncoding(Encoding encoding) {
        checkNotNull(encoding, "The 'encoding' must not be null");
        this.attachmentEncoding = encoding;
        return this;
    }

    @Override
    public Mail id(String id) {
        checkNotNull(id, "The 'id' can't be null");
        this.id = id;
        return this;
    }

    @Override
    public Mail priority(int priority) {
        this.priority = priority;
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
        return address(AddressType.TO, address, personal);
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
        return address(AddressType.CC, address, personal);
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
        return address(AddressType.BCC, address, personal);
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
        this.subject = subject;
        return this;
    }

    @Override
    public Mail header(String name, String value) {
        checkNotNull(name, "The 'name' must not be null");
        checkNotNull(value, "The 'value' must not be null");
        headers.put(name, value);
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
        if (!isMultipart() && !bodies.isEmpty() && !bodies.containsKey(type)) {
            checkMultipart();
        }
        bodies.put(type, value);
        return this;
    }

    @Override
    public Mail attachment(InputStreamSupplier content, String name, @Nullable String type) {
        return attachment(Attachment.Type.REGULAR, content, null, name, type);
    }

    @Override
    public Mail attachment(InputStreamSupplier content, String name) {
        return attachment(content, name, null);
    }

    @Override
    public Mail inline(InputStreamSupplier content, String id, @Nullable String type) {
        return attachment(Attachment.Type.INLINE, content, id, null, type);
    }

    private Mail attachment(Attachment.Type type, InputStreamSupplier content, @Nullable String id,
                            @Nullable String name, @Nullable String contentType) {
        checkNotNull(content, "The 'content' can't be null");
        if (type == Attachment.Type.INLINE) {
            checkNotNull(id, "The 'id' can't be null");
        } else {
            checkNotNull(name, "The 'name' can't be null");
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        checkMultipart();
        List<Attachment> list = attachments.get(type);
        if (list == null) {
            list = new ArrayList<>();
            attachments.put(type, list);
        }
        list.add(new Attachment(type, content, id, name, contentType));

        return this;
    }

    @Override
    public Mail inline(InputStreamSupplier content, String id) {
        return inline(content, id, "application/octet-stream");
    }

    @Override
    public Mail template(String name, @Nullable Locale locale, Map<String, ?> context) {
        checkNotNull(name, "The 'name' can't be null");
        checkNotNull(context, "The 'context' can't be null");
        javamail.applyTemplate(this, name, locale, context);
        return this;
    }

    @Override
    public Mail template(String name, Map<String, ?> context) {
        return template(name, null, context);
    }

    @Override
    public Mail template(String name, @Nullable Locale locale, Object... context) {
        checkNotNull(name, "The 'name' can't be null");
        checkNotNull(context, "The 'context' can't be null");
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
            contextMap = Collections.emptyMap();
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
    public Mail writeTo(Path path) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            return writeTo(os);
        } catch (IOException e) {
            throw new MailIOException(e);
        }
    }

    @Override
    public Mail writeTo(String path) {
        return writeTo(Paths.get(path));
    }

    private Mail address(AddressType type, InternetAddress address) {
        List<InternetAddress> l = addresses.get(type);
        if (l == null) {
            l = new ArrayList<>();
            addresses.put(type, l);
        }
        l.add(address);
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

    private class MimeMessageBuilder {

        private MimeMessage root;
        private final Map<Attachment.Type, MimeMultipart> attachmentParts = new EnumMap<>(Attachment.Type.class);

        private void buildParts() throws MessagingException {
            root = new MimeMessage((Session) null);
            MimeMultipart attachmentPart, inlinePart;
            switch (multipartMode) {
                case NONE:
                    attachmentPart = null;
                    inlinePart = null;
                    break;
                case MIXED:
                    attachmentPart = new MimeMultipart("mixed");
                    root.setContent(attachmentPart);
                    inlinePart = attachmentPart;
                    break;
                case RELATED:
                    attachmentPart = new MimeMultipart("related");
                    root.setContent(attachmentPart);
                    inlinePart = attachmentPart;
                    break;
                case MIXED_RELATED:
                    attachmentPart = new MimeMultipart("mixed");
                    root.setContent(attachmentPart);
                    inlinePart = new MimeMultipart("related");
                    MimeBodyPart relatedBodyPart = new MimeBodyPart();
                    relatedBodyPart.setContent(inlinePart);
                    attachmentPart.addBodyPart(relatedBodyPart);
                    break;
                default:
                    throw new AssertionError("Unhandled MultipartMode: " + multipartMode);
            }
            attachmentParts.put(Attachment.Type.REGULAR, attachmentPart);
            attachmentParts.put(Attachment.Type.INLINE, inlinePart);
        }

        private void buildAddresses() throws MessagingException {
            for (Map.Entry<AddressType, List<InternetAddress>> entry: addresses.entrySet()) {
                List<InternetAddress> l = entry.getValue();
                switch (entry.getKey()) {
                    case FROM:
                        root.removeHeader("From");
                        if (!l.isEmpty()) {
                            root.addFrom(Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case REPLY_TO:
                        if (!l.isEmpty()) {
                            root.setReplyTo(Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case TO:
                        root.removeHeader("To");
                        if (!l.isEmpty()) {
                            root.addRecipients(Message.RecipientType.TO, Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case CC:
                        root.removeHeader("CC");
                        if (!l.isEmpty()) {
                            root.addRecipients(Message.RecipientType.CC, Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    case BCC:
                        root.removeHeader("BCC");
                        if (!l.isEmpty()) {
                            root.addRecipients(Message.RecipientType.BCC, Utils.toArray(l, InternetAddress.class));
                        }
                        break;

                    default:
                        throw new AssertionError("Unhandled RecipientType: " + entry.getKey());
                }
            }
        }

        private MimeBodyPart getOrCreateTextPart() throws MessagingException {
            MimeMultipart container = attachmentParts.get(Attachment.Type.INLINE);
            if (container.getCount() == 0) {
                MimeBodyPart part = new MimeBodyPart();
                container.addBodyPart(part);
                return part;
            } else {
                return (MimeBodyPart) container.getBodyPart(0);
            }
        }

        private void setText(String plain, String html) throws MessagingException {
            MimeMultipart messageBody = new MimeMultipart("alternative");
            getOrCreateTextPart().setContent(messageBody, "text/alternative");

            // Create the plain text part of the message.
            MimeBodyPart plainTextPart = new MimeBodyPart();
            setPlainTextToMimePart(plainTextPart, plain);
            messageBody.addBodyPart(plainTextPart);

            // Create the HTML text part of the message.
            MimeBodyPart htmlTextPart = new MimeBodyPart();
            setHtmlToMimePart(htmlTextPart, html);
            messageBody.addBodyPart(htmlTextPart);
        }

        private void setText(String text, boolean html) throws MessagingException {
            MimePart partToUse;
            if (isMultipart()) {
                partToUse = getOrCreateTextPart();
            } else {
                partToUse = root;
            }
            if (html) {
                setHtmlToMimePart(partToUse, text);
            } else {
                setPlainTextToMimePart(partToUse, text);
            }
        }

        private void setPlainTextToMimePart(MimePart part, String text) throws MessagingException {
            setTextToMimePart(part, text, "text/plain", bodyEncoding);
        }

        private void setHtmlToMimePart(MimePart part, String text) throws MessagingException {
            setTextToMimePart(part, text, "text/html", bodyEncoding);
        }

        private void setTextToMimePart(MimePart mimePart, String text, String type,
                                       @Nullable Encoding encoding) throws MessagingException {
            mimePart.setContent(text, type + "; charset=" + charset.name());
            setContentTransferEncodingHeader(mimePart, encoding);
        }

        private void buildBodies() throws MessagingException {
            String plain = bodies.get(BodyType.PLAIN);
            String html = bodies.get(BodyType.HTML);
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
        }

        private void buildAttachments() throws MessagingException {
            for (Map.Entry<Attachment.Type, List<Attachment>> entry: attachments.entrySet()) {
                MimeMultipart target = attachmentParts.get(entry.getKey());
                for (Attachment att: entry.getValue()) {
                    BodyPart part = att.createBodyPart(attachmentEncoding, charset);
                    target.addBodyPart(part);
                }
            }
        }

        public MimeMessage build() {
            root = null;
            attachmentParts.clear();
            try {
                buildParts();

                try {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        root.addHeader(header.getKey(), MimeUtility.fold(header.getKey().length() + 2,
                                MimeUtility.encodeText(header.getValue(), charset.name(), null)));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Shouldn't happen", e);
                }

                if (priority != null) {
                    root.setHeader("X-Priority", String.valueOf(priority));
                }
                if (subject != null) {
                    root.setSubject(subject, charset.name());
                }

                buildAddresses();
                buildBodies();
                buildAttachments();

                root.saveChanges();

                // Doing this after saveChanges() because the latter overwrites Message-ID header.
                if (id != null) {
                    root.setHeader("Message-ID", '<' + id + '>');
                }

            } catch (MessagingException e) {
                throw Utils.wrapException(e);
            }

            return root;
        }
    }

    private static void setContentTransferEncodingHeader(Part part, @Nullable Encoding encoding) throws MessagingException {
        if (encoding != null) {
            part.setHeader("Content-Transfer-Encoding", checkNotNull(ENCODING_TO_RFC.get(encoding)));
        } else {
            part.removeHeader("Content-Transfer-Encoding");
        }
    }

    private boolean isMultipart() {
        return multipartMode != MultipartMode.NONE;
    }

    private void checkMultipart() {
        if (!isMultipart()) {
            throw new IllegalStateException("This message is not multipart");
        }
    }

    private static class Attachment {

        private enum Type {
            REGULAR, INLINE
        }

        private final Type type;
        private final InputStreamSupplier content;
        private final String id;
        private final String name;
        private final String contentType;

        public Attachment(Type type, InputStreamSupplier content,
                          @Nullable String id, @Nullable String name, String contentType) {
            this.type = type;
            this.content = content;
            this.id = id;
            this.name = name;
            this.contentType = contentType;
        }

        public BodyPart createBodyPart(Encoding encoding, @Nullable Charset charset) throws MessagingException {
            MimeBodyPart r = new MimeBodyPart();
            r.setDisposition(type == Type.INLINE ? MimeBodyPart.INLINE : MimeBodyPart.ATTACHMENT);
            if (id != null) {
                r.setContentID('<' + id + '>');
            }
            if (name != null) {
                checkNotNull(charset);
                try {
                    r.setFileName(MimeUtility.encodeWord(name, charset.name(), null));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("Shouldn't happen", ex);
                }
            }
            r.setDataHandler(new DataHandler(new InputStreamSupplierDatasource(content, contentType,
                    type == Type.INLINE ? "inline" : name)));
            setContentTransferEncodingHeader(r, encoding);
            return r;
        }
    }
}
