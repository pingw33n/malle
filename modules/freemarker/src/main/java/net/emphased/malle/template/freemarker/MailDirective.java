package net.emphased.malle.template.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import net.emphased.malle.AddressType;
import net.emphased.malle.BodyType;
import net.emphased.malle.Encoding;
import net.emphased.malle.Mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class MailDirective implements TemplateDirectiveModel {

    public static final String NAME = "mail";

    private enum TrimMode {
        none, leading, trailing, both
    }

    private static final Map<String, Encoding> STR_TO_ENCODING;
    static {
        Map<String, Encoding> m = new HashMap<>();
        m.put("base64", Encoding.BASE64);
        m.put("quoted-printable", Encoding.QUOTED_PRINTABLE);
        m.put("8bit", Encoding.EIGHT_BIT);
        m.put("7bit", Encoding.SEVEN_BIT);
        m.put("binary", Encoding.BINARY);
        if (Encoding.values().length != m.size()) {
            throw new AssertionError("Not all Encoding values have mappings in STR_TO_ENCODING");
        }
        m.put("auto", null);
        STR_TO_ENCODING = Collections.unmodifiableMap(m);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        doExecute(env, params, loopVars, body);
    }

    private void doExecute(Environment env, Map<String, Object> params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if (loopVars.length != 0) {
            throw new TemplateModelException("'mail' directive doesn't allow loop variables");
        }

        params = new HashMap<>(params);

        String key = getStringParam(params, "key");
        Handler handler = KEY_HANDLERS.get(key);
        if (handler == null) {
            throw new TemplateModelException("Unknown command key passed to 'mail' directive: " + key);
        }

        String bodyStr = body != null ? renderBody(body) : "";

        TrimMode defaultTrimMode;
        if (handler instanceof BodyHandler && ((BodyHandler) handler).getType() == BodyType.PLAIN) {
            defaultTrimMode = TrimMode.trailing;
        } else if (handler instanceof AttachmentHandler) {
            defaultTrimMode = TrimMode.none;
        } else {
            defaultTrimMode = TrimMode.both;
        }
        TrimMode trimMode = getEnumParam(params, "trim", TrimMode.class, defaultTrimMode);
        bodyStr = trim(bodyStr, trimMode);

        Mail m = getMessage(env);
        handler.handle(key, m, bodyStr, params);

        if (!params.isEmpty()) {
            throw new TemplateModelException("Unknown parameters passed to 'mail' (key = '" + key + "') directive: " + params.keySet());
        }
    }

    private String trim(String value, TrimMode mode) {
        switch (mode) {
            case none:
                return value;
            case both:
                return value.trim();
            case leading:
                return value.replaceFirst("^\\s+", "");
            case trailing:
                return value.replaceFirst("\\s+$", "");
            default:
                throw new AssertionError("Unhandled mode: " + mode);

        }
    }

    private static String getStringParam(Map<String, ?> params, String name, String defaultValue) throws TemplateModelException {
        Object o = params.remove(name);
        if (o == null) {
            return defaultValue;
        }
        if (!(o instanceof SimpleScalar)) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be a string");
        }
        return ((SimpleScalar) o).getAsString();
    }

    private static String getStringParam(Map<String, ?> params, String name) throws TemplateModelException {
        return checkParamPresent(getStringParam(params, name, null), name);
    }

    private static <T extends Enum<T>> T getEnumParam(Map<String, ?> params, String name, Class<T> type, T defaultValue)
            throws TemplateModelException {
        String s = getStringParam(params, name, null);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(type, s);
        } catch (IllegalArgumentException e) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be one of: "
                    + Arrays.toString(type.getEnumConstants()), e);
        }
    }

    private static <T extends Enum<T>> T getEnumParam(Map<String, ?> params, String name, Class<T> type) throws TemplateModelException {
        return checkParamPresent(getEnumParam(params, name, type, null), name);
    }

    private static <T> T checkParamPresent(T value, String name) throws TemplateModelException {
        if (value == null) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be present");
        }
        return value;
    }

    private static Encoding getEncodingParam(Map<String, ?> params, String name, Encoding defaultValue) throws TemplateModelException {
        String s = getStringParam(params, name, null);
        if (s == null) {
            return defaultValue;
        }
        Encoding r = STR_TO_ENCODING.get(s);
        if (r == null) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be one of: "
                    + STR_TO_ENCODING.keySet());
        }
        return r;
    }

    private static Charset getCharsetParam(Map<String, ?> params, String name) throws TemplateModelException {
        return checkParamPresent(getCharsetParam(params, name, null), name);
    }

    private static Charset getCharsetParam(Map<String, ?> params, String name, Charset defaultValue) throws TemplateModelException {
        String s = getStringParam(params, name, null);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Charset.forName(s);
        } catch (UnsupportedCharsetException e) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be a valid charset");
        }
    }

    private static Encoding getEncodingParam(Map<String, ?> params, String name) throws TemplateModelException {
        return checkParamPresent(getEncodingParam(params, name, null), name);
    }

    private Mail getMessage(Environment env) throws TemplateModelException {
        return (Mail) ((ObjectModel) env.getDataModel().get(FreemarkerTemplateEngine.MESSAGE_VAR)).getObject();
    }

    private String renderBody(TemplateDirectiveBody body) throws IOException, TemplateException {
        StringWriter content = new StringWriter();
        body.render(content);
        return content.toString();
    }

    private static final Map<String, Handler> KEY_HANDLERS;
    static {
        Map<String, Handler> m = new HashMap<>();
        m.put("from", new AddressHandler(AddressType.FROM));
        m.put("to", new AddressHandler(AddressType.TO));
        m.put("cc", new AddressHandler(AddressType.CC));
        m.put("bcc", new AddressHandler(AddressType.BCC));
        m.put("priority", new PriorityHandler());
        m.put("subject", new SubjectHandler());
        m.put("plain", new BodyHandler(BodyType.PLAIN));
        m.put("html", new BodyHandler(BodyType.HTML));
        m.put("attachment", new AttachmentHandler());

        KEY_HANDLERS = Collections.unmodifiableMap(m);
    }

    private interface Handler {

        void handle(String key, Mail m, String body, Map<String, ?> params) throws TemplateModelException;
    }

    private static abstract class CharsetAwareHandler implements Handler {

        @Override
        public void handle(String key, Mail m, String body, Map<String, ?> params) throws TemplateModelException {
            Charset charset = getCharsetParam(params, "charset", Mail.DEFAULT_CHARSET);
            m.charset(charset);
        }
    }

    private static class SubjectHandler extends CharsetAwareHandler {

        @Override
        public void handle(String key, Mail m, String body, Map<String, ?> params) throws TemplateModelException {
            super.handle(key, m, body, params);
            m.subject(body);
        }
    }

    private static class BodyHandler extends CharsetAwareHandler {

        private final BodyType type;

        public BodyHandler(BodyType type) {
            this.type = type;
        }

        public BodyType getType() {
            return type;
        }

        @Override
        public void handle(String key, Mail m, String body, Map<String, ?> params) throws TemplateModelException {
            super.handle(key, m, body, params);
            Encoding encoding = getEncodingParam(params, "encoding", Mail.DEFAULT_BODY_ENCODING);
            m.bodyEncoding(encoding)
             .body(type, body);
        }
    }

    private static class PriorityHandler implements Handler {

        @Override
        public void handle(String key, Mail m, String body, Map<String, ?> params) throws TemplateModelException {
            int priority;
            try {
                priority = Integer.valueOf(body);
            } catch (NumberFormatException e) {
                throw new TemplateModelException("'mail' directive requires body to be a number", e);
            }
            m.priority(priority);
        }
    }

    private static class AddressHandler extends CharsetAwareHandler {

        private final AddressType type;


        public AddressHandler(AddressType type) {
            this.type = type;
        }

        @Override
        public void handle(String key, Mail m, String body, Map<String, ?> params)
                throws TemplateModelException {
            super.handle(key, m, body, params);
            String address = getStringParam(params, "address", null);
            if (address != null) {
                m.address(type, address, body.isEmpty() ? null : body);
            } else {
                m.address(type, body);
            }
        }
    }

    private static class AttachmentHandler implements Handler {

        @Override
        public void handle(String key, Mail m, String body, Map<String, ?> params)
                throws TemplateModelException {
            String filename = getStringParam(params, "filename");
            String type = getStringParam(params, "type", null);

            Encoding encoding = getEncodingParam(params, "encoding", Mail.DEFAULT_ATTACHMENT_ENCODING);
            if (encoding == null) {
                throw new UnsupportedOperationException("Autodetected encoding is not supported for attachments");
            }
            m.attachmentEncoding(encoding);

            ByteArrayInputStream content = new ByteArrayInputStream(body.getBytes(Charset.forName("UTF-8")));

            if (type != null) {
                m.attachment(content, filename, type);
            } else {
                m.attachment(content, filename);
            }
        }
    }
}
