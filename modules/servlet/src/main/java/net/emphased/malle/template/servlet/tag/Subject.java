package net.emphased.malle.template.servlet.tag;

import javax.servlet.jsp.JspException;
import java.io.IOException;

import static net.emphased.malle.util.Preconditions.checkNotNull;

public class Subject extends Base {

    private String value;

    public Subject() {
        super(TrimMode.both);
    }

    @Override
    public void doTag() throws JspException, IOException {
        getMail().subject(checkNotNull(value));
    }

    public void setValue(String value) {
        this.value = value;
    }
}
