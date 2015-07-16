package net.emphased.malle;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public interface Mail {

    Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    Encoding DEFAULT_BODY_ENCODING = null;
    Encoding DEFAULT_ATTACHMENT_ENCODING = Encoding.BASE64;

    Mail charset(Charset charset);
    Mail charset(String charset);
    Mail bodyEncoding(@Nullable Encoding encoding);
    Mail attachmentEncoding(@Nullable Encoding encoding);

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

    Mail attachment(InputStreamSupplier content, String filename, @Nullable String type);
    Mail attachment(InputStreamSupplier content, String filename);
    Mail inline(InputStreamSupplier content, String id, @Nullable String type);
    Mail inline(InputStreamSupplier content, String id);

    Mail template(String name, @Nullable Locale locale, @Nullable Map<String, ?> context);
    Mail template(String name, @Nullable Locale locale, Object... context);
    Mail template(String name, @Nullable Map<String, ?> context);
    Mail template(String name, Object... context);

    Mail send();

    Mail writeTo(OutputStream outputStream);
    Mail writeTo(Path path);
}
