package net.emphased.malle.template.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import net.emphased.malle.*;
import net.emphased.malle.support.InputStreamSuppliers;

import javax.annotation.Nullable;
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

    private static final Map<String, Handler> CMD_HANDLERS;
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
        m.put("attachment", new AttachmentHandler(false));
        m.put("inline", new AttachmentHandler(true));

        CMD_HANDLERS = Collections.unmodifiableMap(m);
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
    public void execute(Environment env, Map params, TemplateModel[] loopVars, @Nullable TemplateDirectiveBody body)
            throws TemplateException, IOException {
        doExecute(env, params, loopVars, body);
    }

    private void doExecute(Environment env, Map<String, Object> params, TemplateModel[] loopVars,
                           @Nullable TemplateDirectiveBody body) throws TemplateException, IOException {
        if (loopVars.length != 0) {
            throw new TemplateModelException("'mail' directive doesn't allow loop variables");
        }

        params = new HashMap<>(params);

        String cmd = getStringParam(params, "cmd");
        Handler handler = CMD_HANDLERS.get(cmd);
        if (handler == null) {
            throw new TemplateModelException("Unknown command passed to 'mail' directive: " + cmd);
        }

        String bodyStr = body != null ? renderBody(body) : null;

        TrimMode defaultTrimMode;
        if (handler instanceof BodyHandler && ((BodyHandler) handler).getType() == BodyType.PLAIN) {
            defaultTrimMode = TrimMode.trailing;
        } else if (handler instanceof AttachmentHandler) {
            defaultTrimMode = TrimMode.none;
        } else {
            defaultTrimMode = TrimMode.both;
        }
        TrimMode trimMode = getEnumParam(params, "trim", TrimMode.class, defaultTrimMode);
        if (bodyStr != null) {
            bodyStr = trim(bodyStr, trimMode);
        }

        Mail m = getMessage(env);
        handler.handle(cmd, m, bodyStr, params);

        if (!params.isEmpty()) {
            throw new TemplateModelException("Unknown parameters passed to 'mail' (cmd = '" + cmd + "') directive: " + params.keySet());
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

    private static String getStringParam(Map<String, ?> params, String name,
                                         @Nullable String defaultValue) throws TemplateModelException {
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

    private static Integer getIntParam(Map<String, ?> params, String name,
                                       @Nullable Integer defaultValue) throws TemplateModelException {
        String s = getStringParam(params, name, null);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be a valid integer", e);
        }
    }

    private static int getIntParam(Map<String, ?> params, String name) throws TemplateModelException {
        return checkParamPresent(getIntParam(params, name, null), name);
    }

    private static <T extends Enum<T>> T getEnumParam(Map<String, ?> params, String name,
                                                      Class<T> type, @Nullable T defaultValue)
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

    private static <T extends Enum<T>> T getEnumParam(Map<String, ?> params, String name,
                                                      Class<T> type) throws TemplateModelException {
        return checkParamPresent(getEnumParam(params, name, type, null), name);
    }

    private static <T> T checkParamPresent(T value, String name) throws TemplateModelException {
        if (value == null) {
            throw new TemplateModelException("'mail' directive requires '" + name + "' parameter to be present");
        }
        return value;
    }

    private static Encoding getEncodingParam(Map<String, ?> params, String name,
                                             @Nullable Encoding defaultValue) throws TemplateModelException {
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

    private static Charset getCharsetParam(Map<String, ?> params, String name,
                                           @Nullable Charset defaultValue) throws TemplateModelException {
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

    private interface Handler {

        void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params) throws TemplateModelException;
    }

    private static abstract class CharsetAwareHandler implements Handler {

        @Override
        public void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params) throws TemplateModelException {
            Charset charset = getCharsetParam(params, "charset", Mail.DEFAULT_CHARSET);
            m.charset(charset);
        }
    }

    private static class SubjectHandler extends CharsetAwareHandler {

        @Override
        public void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params) throws TemplateModelException {
            super.handle(cmd, m, body, params);
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
        public void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params) throws TemplateModelException {
            super.handle(cmd, m, body, params);
            Encoding encoding = getEncodingParam(params, "encoding", Mail.DEFAULT_BODY_ENCODING);
            m.bodyEncoding(encoding)
             .body(type, body);
        }
    }

    private static class PriorityHandler implements Handler {

        @Override
        public void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params) throws TemplateModelException {
            int priority = getIntParam(params, "value");
            m.priority(priority);
        }
    }

    private static class AddressHandler extends CharsetAwareHandler {

        private final AddressType type;


        public AddressHandler(AddressType type) {
            this.type = type;
        }

        @Override
        public void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params)
                throws TemplateModelException {
            super.handle(cmd, m, body, params);
            String address = getStringParam(params, "address", null);
            if (address != null) {
                m.address(type, address, body);
            } else {
                m.address(type, body);
            }
        }
    }

    private static class AttachmentHandler implements Handler {

        private interface ISSFactory {
            InputStreamSupplier create(String param);
        }

        private static final Map<String, ISSFactory> ISS_FACTORIES;
        static {
            HashMap<String, ISSFactory> m = new HashMap<>();
            m.put("file", new FileISSFactory());
            m.put("resource", new ResourceISSFactory());
            m.put("url", new UrlISSFactory());
            ISS_FACTORIES = Collections.unmodifiableMap(m);
        }

        private final boolean inline;

        public AttachmentHandler(boolean inline) {
            this.inline = inline;
        }

        @Override
        public void handle(String cmd, Mail m, @Nullable String body, Map<String, ?> params)
                throws TemplateModelException {
            String nameOrId = getStringParam(params, inline ? "id" : "name");
            String type = getStringParam(params, "type", null);

            Encoding encoding = getEncodingParam(params, "encoding", Mail.DEFAULT_ATTACHMENT_ENCODING);
            if (encoding == null) {
                throw new TemplateModelException("Encoding auto-detection is not supported for attachments");
            }
            m.attachmentEncoding(encoding);

            String issFactoryName = null;
            InputStreamSupplier content = null;
            for (Map.Entry<String, ISSFactory> issFactory: ISS_FACTORIES.entrySet()) {
                String param = getStringParam(params, issFactory.getKey(), null);
                if (param == null) {
                    continue;
                }
                if (issFactoryName != null) {
                    throw new TemplateModelException("'mail' directive can't have both '" +
                            issFactoryName + "' and '" + issFactory.getKey() + "' parameters set");
                }
                issFactoryName = issFactory.getKey();
                content = issFactory.getValue().create(param);
            }

            if (content == null) {
                if (body == null) {
                    throw new TemplateModelException("'mail' directive must have either resource reference or inline content");
                }
                content = InputStreamSuppliers.bytes(body.getBytes(Charset.forName("UTF-8")));
            } else if (body != null) {
                throw new TemplateModelException("'mail' directive can't have both resource reference and inline content");
            }

            if (inline) {
                m.inline(content, nameOrId, type);
            } else {
                m.attachment(content, nameOrId, type);
            }
        }

        private static class FileISSFactory implements ISSFactory {

            @Override
            public InputStreamSupplier create(String param) {
                return InputStreamSuppliers.file(param);
            }
        }

        private static class ResourceISSFactory implements ISSFactory {

            @Override
            public InputStreamSupplier create(String param) {
                return InputStreamSuppliers.resource(param);
            }
        }

        private static class UrlISSFactory implements ISSFactory {

            @Override
            public InputStreamSupplier create(String param) {
                return InputStreamSuppliers.url(param);
            }
        }
    }
}
