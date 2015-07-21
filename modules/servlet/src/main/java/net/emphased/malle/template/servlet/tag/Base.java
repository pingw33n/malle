package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.Mail;
import net.emphased.malle.template.servlet.ServletTemplateEngine;

import javax.annotation.Nullable;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.StringWriter;

import static net.emphased.malle.util.Preconditions.checkNotNull;

abstract class Base extends SimpleTagSupport {

    protected enum TrimMode {
        none, leading, trailing, both
    }

    private Mail mail;
    private String body;
    private boolean bodyReady;
    private TrimMode trim;

    public Base(TrimMode defaultTrim) {
        this.trim = defaultTrim;
    }

    protected Mail getMail() {
        return mail;
    }

    protected @Nullable String getBody() throws JspException, IOException {
        if (bodyReady) {
            return body;
        }
        JspFragment jspBody = getJspBody();
        if (jspBody == null) {
            body = null;
        } else {
            StringWriter sw = new StringWriter();
            jspBody.invoke(sw);
            body = trim(sw.toString());
        }
        bodyReady = true;
        return body;
    }

    private String trim(String s) {
        switch (trim) {
            case none:
                return trimFirstLineEndings(s, TrimMode.both);
            case both:
                return s.trim();
            case leading:
                return trimFirstLineEndings(s.replaceFirst("^\\s+", ""), TrimMode.trailing);
            case trailing:
                return trimFirstLineEndings(s.replaceFirst("\\s+$", ""), TrimMode.leading);
            default:
                throw new AssertionError("Unhandled mode: " + trim);
        }
    }

    private String trimFirstLineEndings(String s, TrimMode mode) {
        if (mode == TrimMode.both || mode == TrimMode.leading) {
            int i = 0;
            if (s.startsWith("\r\n")) {
                i += 2;
            } else if (s.startsWith("\r") || s.startsWith("\n")) {
                i++;
            }
            s = s.substring(i);
        }

        if (mode == TrimMode.both || mode == TrimMode.trailing) {
            int i = s.length();
            if (s.endsWith("\r\n")) {
                i -= 2;
            } else if (s.endsWith("\r") || s.endsWith("\n")) {
                i -= 1;
            }
            s = s.substring(0, i);
        }

        return s;
    }

    protected TrimMode getTrimEnum() {
        return trim;
    }

    public void setTrim(String trim) {
        this.trim = TrimMode.valueOf(trim);
    }

    @Override
    public void setJspContext(JspContext pc) {
        super.setJspContext(pc);
        init();
    }

    private void init() {
        mail = (Mail) getJspContext().getAttribute(ServletTemplateEngine.MAIL_ATTR, PageContext.REQUEST_SCOPE);
        checkNotNull(mail, "The '%s' request attribute not found, is the JSP being invoked directly?",
                ServletTemplateEngine.MAIL_ATTR);
    }
}
