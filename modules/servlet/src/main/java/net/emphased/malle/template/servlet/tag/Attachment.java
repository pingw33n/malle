package net.emphased.malle.template.servlet.tag;

public class Attachment extends AttachmentBase {

    private String name;

    public Attachment() {
        super(false);
    }

    @Override
    protected String getNameOrId() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
