package net.emphased.malle.template.servlet.tag;

public class Inline extends AttachmentBase {

    private String id;

    public Inline() {
        super(true);
    }

    @Override
    protected String getNameOrId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
