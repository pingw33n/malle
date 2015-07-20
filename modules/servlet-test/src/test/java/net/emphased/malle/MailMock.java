package net.emphased.malle;

import net.emphased.malle.template.MailTemplateEngine;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

import static net.emphased.malle.util.Preconditions.checkArgument;
import static net.emphased.malle.util.Preconditions.checkNotNull;
import static net.emphased.malle.util.Preconditions.checkState;

public class MailMock implements Mail {

    public abstract class Contextual {

        private final Charset charset;
        private final Encoding bodyEncoding;
        private final Encoding attachmentEncoding;

        public Contextual() {
            this.charset = MailMock.this.charset;
            this.bodyEncoding = MailMock.this.bodyEncoding;
            this.attachmentEncoding = MailMock.this.attachmentEncoding;
        }

        public Charset getCharset() {
            return charset;
        }

        public Encoding getBodyEncoding() {
            return bodyEncoding;
        }

        public Encoding getAttachmentEncoding() {
            return attachmentEncoding;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Contextual)) return false;
            Contextual that = (Contextual) o;
            return Objects.equals(charset, that.charset) &&
                    Objects.equals(bodyEncoding, that.bodyEncoding) &&
                    Objects.equals(attachmentEncoding, that.attachmentEncoding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(charset, bodyEncoding, attachmentEncoding);
        }
    }

    public abstract class Address extends Contextual {
    }

    public class SplitAddress extends Address {

        private final String address;
        private final String personal;

        public SplitAddress(String address, @Nullable String personal) {
            this.address = checkNotNull(address);
            this.personal = personal;
        }

        public String getAddress() {
            return address;
        }

        public String getPersonal() {
            return personal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SplitAddress)) return false;
            if (!super.equals(o)) return false;
            SplitAddress that = (SplitAddress) o;
            return Objects.equals(address, that.address) &&
                    Objects.equals(personal, that.personal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), address, personal);
        }

        @Override
        public String toString() {
            return "SplitAddress{" +
                    "address='" + address + '\'' +
                    ", personal='" + personal + '\'' +
                    '}';
        }
    }

    public class EncodedAddress extends Address {

        private final String value;

        public EncodedAddress(String value) {
            this.value = checkNotNull(value);
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EncodedAddress)) return false;
            if (!super.equals(o)) return false;
            EncodedAddress that = (EncodedAddress) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), value);
        }

        @Override
        public String toString() {
            return "EncodedAddress{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public class Header extends Contextual {

        private final String name;
        private final String value;

        public Header(String name, String value) {
            this.name = checkNotNull(name);
            this.value = checkNotNull(value);
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Header)) return false;
            if (!super.equals(o)) return false;
            Header header = (Header) o;
            return Objects.equals(name, header.name) &&
                    Objects.equals(value, header.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name, value);
        }

        @Override
        public String toString() {
            return "Header{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public class Attachment extends Contextual {

        private final InputStreamSupplier content;
        private final String name;
        private final String type;

        public Attachment(InputStreamSupplier content, String name, @Nullable String type) {
            this.content = checkNotNull(content);
            this.name = checkNotNull(name);
            this.type = type;
        }

        public InputStreamSupplier getContent() {
            return content;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Attachment{" +
                    "content=" + content +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public class Inline extends Contextual {

        private final InputStreamSupplier content;
        private final String id;
        private final String type;

        public Inline(InputStreamSupplier content, String id, @Nullable String type) {
            this.content = checkNotNull(content);
            this.id = checkNotNull(id);
            this.type = type;
        }

        public InputStreamSupplier getContent() {
            return content;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Inline{" +
                    "content=" + content +
                    ", id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    private MailTemplateEngine templateEngine;
    private final boolean multipart;
    private Charset charset = DEFAULT_CHARSET;
    private Encoding bodyEncoding = DEFAULT_BODY_ENCODING;
    private Encoding attachmentEncoding = DEFAULT_ATTACHMENT_ENCODING;
    private String id;
    private int priority;
    private final Map<AddressType, List<Address>> addresses;
    {
        Map<AddressType, List<Address>> m = new EnumMap<>(AddressType.class);
        for (AddressType at: AddressType.values()) {
            m.put(at, new ArrayList<Address>());
        }
        addresses = Collections.unmodifiableMap(m);
    }
    private final Map<BodyType, String> bodies = new EnumMap<>(BodyType.class);
    private String subject;
    private final List<Header> headers = new ArrayList<>();
    private final List<Attachment> attachments = new ArrayList<>();
    private final List<Inline> inlines = new ArrayList<>();

    public MailMock(boolean multipart) {
        this.multipart = multipart;
    }

    @Override
    public Mail charset(Charset charset) {
        this.charset = checkNotNull(charset);
        return this;
    }

    @Override
    public Mail charset(String charset) {
        return charset(Charset.forName(charset));
    }

    @Override
    public Mail bodyEncoding(@Nullable Encoding encoding) {
        bodyEncoding = encoding;
        return this;
    }

    @Override
    public Mail attachmentEncoding(Encoding encoding) {
        attachmentEncoding = checkNotNull(encoding);
        return this;
    }

    @Override
    public Mail id(String id) {
        this.id = checkNotNull(id);
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
        this.addresses.get(type).addAll(toAddressList(addresses));
        return this;
    }

    @Override
    public Mail address(AddressType type, String[] addresses) {
        return address(type, toIterable(addresses));
    }

    @Override
    public Mail address(AddressType type, String addresses) {
        this.addresses.get(type).add(new EncodedAddress(addresses));
        return this;
    }

    @Override
    public Mail address(AddressType type, String address, @Nullable String personal) {
        this.addresses.get(type).add(new SplitAddress(address, personal));
        return this;
    }

    @Override
    public Mail subject(String subject) {
        this.subject = checkNotNull(subject);
        return this;
    }

    @Override
    public Mail header(String name, String value) {
        headers.add(new Header(name, value));
        return this;
    }

    @Override
    public Mail plain(String plain) {
        return body(BodyType.PLAIN, plain);
    }

    @Override
    public Mail html(String html) {
        return body(BodyType.HTML, html);
    }

    @Override
    public Mail body(BodyType type, String value) {
        bodies.put(type, value);
        return this;
    }

    @Override
    public Mail attachment(InputStreamSupplier content, String filename, @Nullable String type) {
        attachments.add(new Attachment(content, filename, type));
        return this;
    }

    @Override
    public Mail attachment(InputStreamSupplier content, String filename) {
        return attachment(content, filename, null);
    }

    @Override
    public Mail inline(InputStreamSupplier content, String id, @Nullable String type) {
        inlines.add(new Inline(content, id, type));
        return this;
    }

    @Override
    public Mail inline(InputStreamSupplier content, String id) {
        return inline(content, id, null);
    }

    @Override
    public Mail template(String name, @Nullable Locale locale, Map<String, ?> context) {
        checkState(templateEngine != null, "The 'templateEngine' must be set first");
        templateEngine.getTemplate(name, locale).apply(this, checkNotNull(context));
        return this;
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
    public Mail template(String name, Map<String, ?> context) {
        return template(name, null, context);
    }

    @Override
    public Mail template(String name, Object... context) {
        return template(name, null, context);
    }

    @Override
    public Mail send() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mail writeTo(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mail writeTo(Path path) {
        throw new UnsupportedOperationException();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public MailTemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(MailTemplateEngine templateEngine) {
        this.templateEngine = checkNotNull(templateEngine);
    }

    public MailMock withTemplateEngine(MailTemplateEngine templateEngine) {
        setTemplateEngine(templateEngine);
        return this;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Encoding getBodyEncoding() {
        return bodyEncoding;
    }

    public void setBodyEncoding(Encoding bodyEncoding) {
        this.bodyEncoding = bodyEncoding;
    }

    public Encoding getAttachmentEncoding() {
        return attachmentEncoding;
    }

    public void setAttachmentEncoding(Encoding attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<AddressType, List<Address>> getAddresses() {
        return addresses;
    }

    public Map<BodyType, String> getBodies() {
        return bodies;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public List<Inline> getInlines() {
        return inlines;
    }

    private List<Address> toAddressList(Iterable<String> addresses) {
        List<Address> r = new ArrayList<>();
        for (String a: addresses) {
            r.add(new EncodedAddress(a));
        }
        return r;
    }

    private static <T> Iterable<T> toIterable(final T[] array) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    private int i;

                    @Override
                    public boolean hasNext() {
                        return i < array.length;
                    }

                    @Override
                    public T next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return array[i++];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailMock mailMock = (MailMock) o;
        return Objects.equals(multipart, mailMock.multipart) &&
                Objects.equals(priority, mailMock.priority) &&
                Objects.equals(templateEngine, mailMock.templateEngine) &&
                Objects.equals(charset, mailMock.charset) &&
                Objects.equals(bodyEncoding, mailMock.bodyEncoding) &&
                Objects.equals(attachmentEncoding, mailMock.attachmentEncoding) &&
                Objects.equals(id, mailMock.id) &&
                Objects.equals(addresses, mailMock.addresses) &&
                Objects.equals(bodies, mailMock.bodies) &&
                Objects.equals(subject, mailMock.subject) &&
                Objects.equals(headers, mailMock.headers) &&
                Objects.equals(attachments, mailMock.attachments) &&
                Objects.equals(inlines, mailMock.inlines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateEngine, multipart, charset, bodyEncoding, attachmentEncoding, id,
                priority, addresses, bodies, subject, headers, attachments, inlines);
    }
}
