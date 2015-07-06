package net.emphased.malle.javamail;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class InputStreamDatasource implements DataSource {

    private final InputStream inputStream;
    private boolean used;
    private final String contentType;
    private final String name;

    public InputStreamDatasource(InputStream inputStream, String contentType, String name) {
        if (inputStream == null) {
            throw new NullPointerException("The 'inputStream' must not be null");
        }
        if (contentType == null) {
            throw new NullPointerException("The 'contentType' must not be null");
        }
        if (name == null) {
            throw new NullPointerException("The 'name' must not be null");
        }

        this.inputStream = inputStream;
        this.contentType = contentType;
        this.name = name;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (used) {
            throw new IllegalStateException("Already used");
        }
        used = true;
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }
}
