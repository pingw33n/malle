package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.BodyType;

import javax.servlet.jsp.JspException;
import java.io.IOException;

abstract class Body extends Base {

    private final BodyType type;

    public Body(BodyType type) {
        this.type = type;
    }

    public BodyType getType() {
        return type;
    }

    @Override
    public void doTag() throws JspException, IOException {
        String body = getBody();
        getMail().body(type, body);
    }
}
