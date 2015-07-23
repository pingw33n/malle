package net.emphased.malle.template.servlet.tag;

import javax.servlet.jsp.JspException;
import java.io.IOException;

public class Subject extends Base {

    public Subject() {
        super(TrimMode.both);
    }

    @Override
    public void doTag() throws JspException, IOException {
        getMail().subject(getNonNullBody());
    }
}
