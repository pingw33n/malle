package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.Mail;
import net.emphased.malle.template.servlet.ServletTemplateEngine;

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

    private TrimMode trim;

    public Base(TrimMode defaultTrim) {
        // Check that the JSP is inside of the ServletTemplateEngine and not being invoked directly.
        getMail();

        this.trim = defaultTrim;
    }

    protected Mail getMail() {
        try {
            Mail r = (Mail) getJspContext().getAttribute(ServletTemplateEngine.MAIL_ATTR, PageContext.REQUEST_SCOPE);
            return checkNotNull(r);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    protected String getBody() throws JspException, IOException {
        JspFragment body = getJspBody();
        if (body == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        body.invoke(sw);
        return trim(sw.toString());
    }

    private String trim(String s) {
        switch (trim) {
            case none:
                return trimFirstLeadingLineEnd(s);
            case both:
                return s.trim();
            case leading:
                return s.replaceFirst("^\\s+", "");
            case trailing:
                return trimFirstLeadingLineEnd(s.replaceFirst("\\s+$", ""));
            default:
                throw new AssertionError("Unhandled mode: " + trim);
        }
    }

    private String trimFirstLeadingLineEnd(String s) {
        if (s.startsWith("\r\n")) {
            return s.substring(2);
        } else if (s.startsWith("\r") || s.startsWith("\n")) {
            return s.substring(1);
        } else {
            return s;
        }
    }

    protected TrimMode getTrimEnum() {
        return trim;
    }

    public void setTrim(String trim) {
        this.trim = TrimMode.valueOf(trim);
    }
}
