package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.Encoding;
import net.emphased.malle.Mail;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Settings extends Base {

    private String charset;
    private String bodyEncoding;
    private String attachmentEncoding;

    public Settings() {
        super(TrimMode.none);
    }

    private static final Map<String, Encoding> STR_TO_ENCODING;
    static {
        Map<String, Encoding> m = new HashMap<>();
        m.put("base64", Encoding.BASE64);
        m.put("quoted-printable", Encoding.QUOTED_PRINTABLE);
        m.put("8bit", Encoding.EIGHT_BIT);
        m.put("7bit", Encoding.SEVEN_BIT);
        m.put("binary", Encoding.BINARY);
        if (Encoding.values().length != m.size()) {
            throw new AssertionError("Not all Encoding values have mappings in STR_TO_ENCODING");
        }
        m.put("auto", null);
        STR_TO_ENCODING = Collections.unmodifiableMap(m);
    }


    @Override
    public void doTag() throws JspException, IOException {
        Mail m = getMail();

        if (charset != null) {
            m.charset(charset);
        }
        if (bodyEncoding != null) {
            m.bodyEncoding(STR_TO_ENCODING.get(bodyEncoding));
        }
        if (attachmentEncoding != null){
            m.attachmentEncoding(STR_TO_ENCODING.get(attachmentEncoding));
        }
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setBodyEncoding(String bodyEncoding) {
        this.bodyEncoding = bodyEncoding;
    }

    public void setAttachmentEncoding(String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
    }
}
