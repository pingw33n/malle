package net.emphased.malle.template.servlet.tag;

import javax.servlet.jsp.JspException;
import java.io.IOException;

import static net.emphased.malle.util.Preconditions.checkNotNull;

public class Priority extends Base {

    private Integer value;

    public Priority() {
        super(TrimMode.both);
    }

    @Override
    public void doTag() throws JspException, IOException {
        getMail().priority(checkNotNull(value));
    }

    public void setValue(int value) {
        this.value = value;
    }
}
