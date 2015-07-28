package net.emphased.malle;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Fluent mail builder. This is the main interface to use when composing and sending a mail message.
 *
 * <p>Depending on whether it's multipart or not (i.e. created with {@link MailSystem#mail(boolean) mail(true)} or
 * {@code mail(false)}) certain methods will throw {@link UnsupportedOperationException}. A non-multipart message
 * doesn't support:
 * <ul>
 * <li>Having both plain and HTML bodies within a single message. See {@link #plain(String)}, {@link #html(String)}, {@link #body(BodyType, String)}.
 * <li>Attachments (regular and inline). See {@link #attachment(InputStreamSupplier, String, String)}, {@link #inline(InputStreamSupplier, String, String)}
 *     (and other variants of these methods).
 * </ul>
 *
 * <p>Minimal usage example:
 * <pre>
 * MailSystem mailSystem = ... ;
 *
 * mailSystem.mail()
 *           .from("me@example.com")
 *           .to("you@example.com")
 *           .subject("Hello there")
 *           .html("&lt;p>Hey hey hey&lt;/p>")
 *           .send();
 * </pre>
 *
 * @see MailSystem
 */
public interface Mail {

    /**
     * The default value ("UTF-8") for {@link #charset(Charset) charset}.
     */
    Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * The default value ({@code null} - auto-detect) for {@link #bodyEncoding(Encoding) bodyEncoding}.
     */
    Encoding DEFAULT_BODY_ENCODING = null;

    /**
     * The default value ({@link Encoding#BASE64}) for {@link #attachmentEncoding(Encoding) attachmentEncoding}.
     */
    Encoding DEFAULT_ATTACHMENT_ENCODING = Encoding.BASE64;

    /**
     * Sets the charset. This charset is used when encoding headers and as the {@code charset} parameter
     * in the body content type ({@code text/plain; charset=...}, {@code text/html; charset=...}). The default value is
     * {@link #DEFAULT_CHARSET}.
     */
    Mail charset(Charset charset);

    /**
     * Shortcut for {@link #charset(Charset) charset(Charset.forName(charset))}.
     * @throws IllegalCharsetNameException
     * @throws UnsupportedCharsetException
     * @see Charset#forName(String)
     */
    Mail charset(String charset) throws IllegalCharsetNameException, UnsupportedCharsetException;

    /**
     * Sets the body encoding. This encoding is used only when encoding a body. For attachments there's a separate
     * encoding setting.
     * @param encoding If {@code null} the encoding will be auto-detected in order to optimize the encoded content size.
     * @see #attachmentEncoding(Encoding)
     */
    Mail bodyEncoding(@Nullable Encoding encoding);

    /**
     * Sets the attachment encoding. This encoding is used when encoding an attachment. For bodies there's a separate
     * encoding setting.
     * @param encoding Must not be {@code null} since the auto-detection is not supported for attachments.
     * @see #bodyEncoding(Encoding)
     */
    Mail attachmentEncoding(Encoding encoding);

    /**
     * Sets the {@code Message-ID} header.
     * @param id the ID string without the surrounding angle brackets {@code <...>}
     */
    Mail id(String id);

    /**
     * Sets the {@code X-Priority} header.
     * @param priority the priority value from 1 (highest priority) to 5 (lowest priority)
     */
    Mail priority(int priority);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.FROM, address)} for each
     * element in the {@code addresses} iterable.
     */
    Mail from(Iterable<String> addresses);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.FROM, address)} for each
     * element in the {@code addresses} array.
     */
    Mail from(String[] addresses);

    /**
     * Shortcut for {@link #address(AddressType, String) address(AddressType.FROM, addresses)}.
     */
    Mail from(String addresses);

    /**
     * Shortcut for {@link #address(AddressType, String, String) address(AddressType.FROM, address, personal)}.
     */
    Mail from(String address, @Nullable String personal);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.REPLY_TO, address)} for each
     * element in the {@code addresses} iterable.
     */
    Mail replyTo(Iterable<String> addresses);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.REPLY_TO, address)} for each
     * element in the {@code addresses} array.
     */
    Mail replyTo(String[] addresses);

    /**
     * Shortcut for {@link #address(AddressType, String) address(AddressType.REPLY_TO, addresses)}.
     */
    Mail replyTo(String addresses);

    /**
     * Shortcut for {@link #address(AddressType, String, String) address(AddressType.REPLY_TO, address, personal)}.
     */
    Mail replyTo(String address, @Nullable String personal);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.TO, address)} for each
     * element in the {@code addresses} iterable.
     */
    Mail to(Iterable<String> addresses);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.TO, address)} for each
     * element in the {@code addresses} array.
     */
    Mail to(String[] addresses);

    /**
     * Shortcut for {@link #address(AddressType, String) address(AddressType.TO, addresses)}.
     */
    Mail to(String addresses);

    /**
     * Shortcut for {@link #address(AddressType, String, String) address(AddressType.TO, address, personal)}.
     */
    Mail to(String address, @Nullable String personal);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.CC, address)} for each
     * element in the {@code addresses} iterable.
     */
    Mail cc(Iterable<String> addresses);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.CC, address)} for each
     * element in the {@code addresses} array.
     */
    Mail cc(String[] addresses);

    /**
     * Shortcut for {@link #address(AddressType, String) address(AddressType.CC, addresses)}.
     */
    Mail cc(String addresses);

    /**
     * Shortcut for {@link #address(AddressType, String, String) address(AddressType.CC, address, personal)}.
     */
    Mail cc(String address, @Nullable String personal);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.BCC, address)} for each
     * element in the {@code addresses} iterable.
     */
    Mail bcc(Iterable<String> addresses);

    /**
     * Identical to calling {@link #address(AddressType, String) address(AddressType.BCC, address)} for each
     * element in the {@code addresses} array.
     */
    Mail bcc(String[] addresses);

    /**
     * Shortcut for {@link #address(AddressType, String) address(AddressType.BCC, addresses)}.
     */
    Mail bcc(String addresses);

    /**
     * Shortcut for {@link #address(AddressType, String, String) address(AddressType.BCC, address, personal)}.
     */
    Mail bcc(String address, @Nullable String personal);

    /**
     * Identical to calling {@link #address(AddressType, String) address(type, address)} for each
     * element in the {@code addresses} iterable.
     */
    Mail address(AddressType type, Iterable<String> addresses);

    /**
     * Identical to calling {@link #address(AddressType, String) address(type, address)} for each
     * element in the {@code addresses} array.
     */
    Mail address(AddressType type, String[] addresses);

    /**
     * Adds the specified {@code addresses} to the address header specified by {@code type}.
     * If needed the personal parts will be encoded according to the RFC822 using the {@link #charset(Charset) charset}.
     *
     * <p>Some examples:
     *
     * <pre>
     *     .address(AddressType.FROM, "from@example.com")
     *     .address(AddressType.TO, "John Doe &lt;to@example.com>")
     *     .address(AddressType.CC, "♡ Unicode ♡ &lt;cc1@example.com>, cc2@example.com")
     *     .address(AddressType.CC, "cc1@example.com")
     *     .address(AddressType.CC, "cc2@example.com")
     * </pre>
     * @param addresses one or more addresses encoded according to RFC822.
     *                  Unicode personal part doesn't have to be encoded (see examples above).
     */
    Mail address(AddressType type, String addresses);

    /**
     * Adds a single {@code address} to the address header specified by {@code type}. An optional {@code personal}
     * may be specified and if needed it will be encoded according to the RFC822 using the {@link #charset(Charset) charset}.
     *
     * <p>Note that this method is different from {@link #address(AddressType, String)} in that the {@code address}
     * must be a single {@code local@domain} address without the personal part (it may be specified as the {@code personal} parameter).
     */
    Mail address(AddressType type, String address, @Nullable String personal);

    /**
     * Sets the {@code Subject} header. If needed the value will be encoded according to the RFC822 using the
     * {@link #charset(Charset) charset}.
     */
    Mail subject(String subject);

    /**
     * Sets the header specified with {@code name}. If needed the {@code value} will be encoded according to the RFC822 using the
     * {@link #charset(Charset) charset}.
     */
    Mail header(String name, String value);

    /**
     * Shortcut for {@link #body(BodyType, String) body(BodyType.PLAIN, plain}.
     */
    Mail plain(String plain);

    /**
     * Shortcut for {@link #body(BodyType, String) body(BodyType.HTML, html}.
     */
    Mail html(String html);

    /**
     * Adds a body of the specified {@code type}. If the body of this type already exists it's overwritten with the new
     * {@code value}. The {@link #bodyEncoding(Encoding) bodyEncoding} will be used to encode the body content.
     *
     * @throws UnsupportedOperationException if this is a non-multipart mail and both body types exist
     */
    Mail body(BodyType type, String value) throws UnsupportedOperationException;

    /**
     * Adds an attachment. The {@link #attachmentEncoding(Encoding) attachmentEncoding} will be used to encode the attachment content.
     * @param name the name that will appear as attachment filename in a mail client
     * @param type the content type. If {@code null} it will be auto-detected.
     * @throws UnsupportedOperationException is this is a non-multipart mail
     * @see net.emphased.malle.support.InputStreamSuppliers
     */
    Mail attachment(InputStreamSupplier content, String name, @Nullable String type)
            throws UnsupportedOperationException;

    /**
     * Shortcut for {@link #attachment(InputStreamSupplier, String, String) attachment(content, name, null}.
     */
    Mail attachment(InputStreamSupplier content, String name);

    /**
     * Adds an inline attachment. The {@link #attachmentEncoding(Encoding) attachmentEncoding} will be used to encode
     * the attachment content.
     * @param id the ID string without the surrounding angle brackets {@code <...>}
     * @param type the content type. If {@code null} it will be auto-detected.
     * @throws UnsupportedOperationException is this is a non-multipart mail
     * @see net.emphased.malle.support.InputStreamSuppliers
     */
    Mail inline(InputStreamSupplier content, String id, @Nullable String type)
            throws UnsupportedOperationException;

    /**
     * Shortcut for {@link #inline(InputStreamSupplier, String, String) inline(content, name, null}.
     */
    Mail inline(InputStreamSupplier content, String id);

    /**
     * Applies the template specified by {@code name}.
     * @param name the template name. The semantics is
     * {@linkplain net.emphased.malle.template.MailTemplateEngine template engine} specific.
     * @param locale optional locale to render the template in. If {@code null} the default locale is used.
     */
    Mail template(String name, @Nullable Locale locale, Map<String, ?> context);

    /**
     * A version of {@link #template(String, Locale, Map)} with the {@code context} passed as varargs parameter.
     *
     * <p>The following code
     *
     * <pre>
     *     Mail m = ...;
     *     m.template("template name", null,
     *                "key1", "value1",
     *                "key2", "value2");
     * </pre>
     *
     * does the same as
     *
     * <pre>
     *     Map&lt;String, Object> context = new LinkedHashMap&lt;>();
     *     context.put("key1", "value1");
     *     context.put("key2", "value2");
     *
     *     Mail m = ...;
     *     m.template("template name", null, context);
     * </pre>
     *
     * @param context a flattened map in a form of a (key, value) pair sequence
     * @throws IllegalArgumentException if the length of the the {@code context} array is not even
     * @throws IllegalArgumentException if a {@code key} element in the {@code context} array is not a {@code String}
     */
    Mail template(String name, @Nullable Locale locale, Object... context)
            throws IllegalArgumentException;

    /**
     * Shortcut for {@link #template(String, Locale, Map) template(name, null, context)}.
     */
    Mail template(String name, Map<String, ?> context);

    /**
     * Shortcut for {@link #template(String, Locale, Object...) template(name, null, context)}.
     */
    Mail template(String name, Object... context);

    /**
     * Builds and sends out this mail.
     */
    Mail send();

    /**
     * Builds and writes this mail as an RFC822 data to the {@code outputStream}.
     */
    Mail writeTo(OutputStream outputStream);

    /**
     * Builds and writes this mail as an RFC822 data to the file specified by {@code path}.
     */
    Mail writeTo(Path path);

    /**
     * Shortcut for {@link #writeTo(Path) writeTo(Paths.get(path))}.
     */
    Mail writeTo(String path);
}
