package net.emphased.malle.javamail;

import net.emphased.malle.InputStreamSupplier;
import net.emphased.malle.MailIOException;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static net.emphased.malle.util.Preconditions.checkNotNull;

class InputStreamSupplierDatasource implements DataSource {

    private final InputStreamSupplier inputStreamSupplier;
    private final String contentType;
    private final String name;

    public InputStreamSupplierDatasource(InputStreamSupplier inputStreamSupplier, String contentType, String name) {
        checkNotNull(inputStreamSupplier, "The 'inputStreamSupplier' must not be null");
        checkNotNull(contentType, "The 'contentType' must not be null");
        checkNotNull(name, "The 'name' must not be null");
        this.inputStreamSupplier = inputStreamSupplier;
        this.contentType = contentType;
        this.name = name;
    }

    public InputStreamSupplier getInputStreamSupplier() {
        return inputStreamSupplier;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return inputStreamSupplier.get();
        } catch (MailIOException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw e;
            }
        }
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
