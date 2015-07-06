package net.emphased.malle;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface MailMessage {

    Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    Encoding DEFAULT_BODY_ENCODING = Encoding.AUTO;
    Encoding DEFAULT_ATTACHMENT_ENCODING = Encoding.BASE64;

    MailMessage charset(Charset charset);
    MailMessage charset(String charset);
    MailMessage bodyEncoding(Encoding encoding);
    MailMessage attachmentEncoding(Encoding encoding);

    MailMessage id(String id);
    MailMessage priority(int priority);

    MailMessage from(Iterable<String> addresses);
    MailMessage from(String[] addresses);
    MailMessage from(String addresses);
    MailMessage from(String address, @Nullable String personal);
    MailMessage to(Iterable<String> addresses);
    MailMessage to(String[] addresses);
    MailMessage to(String addresses);
    MailMessage to(String address, @Nullable String personal);
    MailMessage cc(Iterable<String> addresses);
    MailMessage cc(String[] addresses);
    MailMessage cc(String address, @Nullable String personal);
    MailMessage cc(String addresses);
    MailMessage bcc(Iterable<String> addresses);
    MailMessage bcc(String[] addresses);
    MailMessage bcc(String address, @Nullable String personal);
    MailMessage bcc(String addresses);
    MailMessage address(AddressType type, Iterable<String> addresses);
    MailMessage address(AddressType type, String[] addresses);
    MailMessage address(AddressType type, String addresses);
    MailMessage address(AddressType type, String address, @Nullable String personal);

    MailMessage subject(String subject);

    MailMessage header(String name, String value);
    MailMessage header(String name, String pattern, Object... words);

    MailMessage plain(String plain);
    MailMessage html(String html);
    MailMessage body(BodyType type, String value);

    MailMessage attachment(InputStream content, String filename, String type);
    MailMessage attachment(InputStream content, String filename);
    MailMessage inline(InputStream content, String id, String type);

    void send();
}
