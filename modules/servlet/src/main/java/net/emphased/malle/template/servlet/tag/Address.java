package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.AddressType;

import javax.servlet.jsp.JspException;
import java.io.IOException;

import static net.emphased.malle.util.Preconditions.checkArgument;

abstract class Address extends Base {

    private final AddressType type;
    private String tagName;
    private String address;
    private String personal;

    public Address(AddressType type) {
        super(TrimMode.both);
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

        if (address != null) {
            checkArgument(!(body != null && personal != null), "[%s] Either body or 'personal' may be specified but not both", getTagName());
            String personal = body != null ? body : this.personal;
            getMail().address(type, address, personal);
        } else {
            checkArgument(body != null && personal == null, "[%s] When 'address' attribute is not specified the body must provide " +
                    "a valid list of addresses with optional personal part. This usage type also forbids specifying 'personal' attribute", getTagName());
            getMail().address(type, body);
        }
    }

    private String getTagName() {
        if (tagName == null) {
            tagName = type.name().replace('_', '-').toLowerCase();
        }
        return tagName;
    }
}
