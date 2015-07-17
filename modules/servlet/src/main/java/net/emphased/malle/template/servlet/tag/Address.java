package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.AddressType;
import net.emphased.malle.Mail;

import javax.servlet.jsp.JspException;
import java.io.IOException;

abstract class Address extends Base {

    private final AddressType type;
    private String address;
    private String personal;

    public Address(AddressType type) {
        this.type = type;
    }

    public AddressType getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    @Override
    public void doTag() throws JspException, IOException {
        String body = getBody();
        boolean hasBody = !body.isEmpty();

        String personal;
        if (!hasBody) {
            personal = this.personal;
        } else if (this.personal != null) {
            throw new IllegalArgumentException("Either body or 'personal' must be set");
        } else {
            personal = body;
        }

        Mail mail = getMail();
        mail.address(type, address, personal);
    }
}
