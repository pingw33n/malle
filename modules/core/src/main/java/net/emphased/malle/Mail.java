package net.emphased.malle;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.charset.Charset;
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

    Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    Encoding DEFAULT_BODY_ENCODING = null;
    Encoding DEFAULT_ATTACHMENT_ENCODING = Encoding.BASE64;

    Mail charset(Charset charset);
    Mail charset(String charset);
    Mail bodyEncoding(@Nullable Encoding encoding);
    Mail attachmentEncoding(Encoding encoding);

    Mail id(String id);
    Mail priority(int priority);

    Mail from(Iterable<String> addresses);
    Mail from(String[] addresses);
    Mail from(String addresses);
    Mail from(String address, @Nullable String personal);
    Mail replyTo(Iterable<String> addresses);
    Mail replyTo(String[] addresses);
    Mail replyTo(String addresses);
    Mail replyTo(String address, @Nullable String personal);
    Mail to(Iterable<String> addresses);
    Mail to(String[] addresses);
    Mail to(String addresses);
    Mail to(String address, @Nullable String personal);
    Mail cc(Iterable<String> addresses);
    Mail cc(String[] addresses);
    Mail cc(String address, @Nullable String personal);
    Mail cc(String addresses);
    Mail bcc(Iterable<String> addresses);
    Mail bcc(String[] addresses);
    Mail bcc(String address, @Nullable String personal);
    Mail bcc(String addresses);
    Mail address(AddressType type, Iterable<String> addresses);
    Mail address(AddressType type, String[] addresses);
    Mail address(AddressType type, String addresses);
    Mail address(AddressType type, String address, @Nullable String personal);

    Mail subject(String subject);

    Mail header(String name, String value);

    Mail plain(String plain);
    Mail html(String html);
    Mail body(BodyType type, String value);

    Mail attachment(InputStreamSupplier content, String name, @Nullable String type);
    Mail attachment(InputStreamSupplier content, String name);
    Mail inline(InputStreamSupplier content, String id, @Nullable String type);
    Mail inline(InputStreamSupplier content, String id);

    Mail template(String name, @Nullable Locale locale, Map<String, ?> context);
    Mail template(String name, @Nullable Locale locale, Object... context);
    Mail template(String name, Map<String, ?> context);
    Mail template(String name, Object... context);

    Mail send();

    Mail writeTo(OutputStream outputStream);
    Mail writeTo(Path path);
    Mail writeTo(String path);
}
