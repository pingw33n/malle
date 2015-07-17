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

    protected Mail getMail() {
        Mail r = (Mail) getJspContext().getAttribute(ServletTemplateEngine.MAIL_ATTR, PageContext.REQUEST_SCOPE);
        return checkNotNull(r);
    }

    protected String getBody() throws JspException, IOException {
        JspFragment body = getJspBody();
        if (body == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        body.invoke(sw);
        return sw.toString();
    }
}
