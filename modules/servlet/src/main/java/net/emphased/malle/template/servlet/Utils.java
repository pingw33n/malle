package net.emphased.malle.template.servlet;

class Utils {

    public static final boolean JSTL_EXISTS = isClassExists("javax.servlet.jsp.jstl.core.Config");
    public static final String JSTL_FMT_LOCALE = "javax.servlet.jsp.jstl.fmt.locale";

    public static boolean isClassExists(String name) {
        try {
            Class.forName(name, false, Utils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException unused) {
            return false;
        }
    }


    private Utils() {}
}
