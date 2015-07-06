package net.emphased.malle.template.freemarker;

import freemarker.template.TemplateModel;

class ObjectModel implements TemplateModel {

    private final Object object;

    public ObjectModel(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
