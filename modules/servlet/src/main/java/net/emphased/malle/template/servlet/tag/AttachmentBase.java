package net.emphased.malle.template.servlet.tag;

import net.emphased.malle.InputStreamSupplier;
import net.emphased.malle.Mail;

import javax.servlet.jsp.JspException;
import java.io.IOException;

import static net.emphased.malle.support.InputStreamSuppliers.*;
import static net.emphased.malle.util.Preconditions.checkArgument;

abstract class AttachmentBase extends Base {

    private final boolean inline;
    private String type;
    private String file;
    private String url;
    private String resource;

    public AttachmentBase(boolean inline) {
        super(TrimMode.none);
        this.inline = inline;
    }

    @Override
    public void doTag() throws JspException, IOException {
        InputStreamSupplier iss = getInputStreamSupplier();
        String nameOrId = getNameOrId();
        Mail mail = getMail();
        if (inline) {
            mail.inline(iss, nameOrId, type);
        } else {
            mail.attachment(iss, nameOrId, type);
        }
    }

    private InputStreamSupplier getInputStreamSupplier() throws IOException, JspException {
        checkArgument(i(file != null) + i(url != null) + i(resource != null) == 1,
                "[%s] Either 'file', 'url', 'resource' or body may be specified", getTagName());

        if (file != null) {
            return file(file);
        } else if (url != null) {
            return url(url);
        } else if (resource != null) {
            return resource(resource);
        } else {
            throw new AssertionError("Shouldn't happen");
        }
    }

    private String getTagName() {
        return inline ? "inline" : "attachment";
    }

    private static int i(boolean b) {
        return b ? 1 : 0;
    }

    protected abstract String getNameOrId();

    public void setType(String type) {
        this.type = type;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
