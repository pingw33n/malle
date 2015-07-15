package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;
import net.emphased.malle.MailIOException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public final class InputStreamSuppliers {

    public static InputStreamSupplier bytes(byte[] bytes, int offset, int size) {
        return new ByteArrayInputStreamSupplier(bytes, offset, size);
    }

    public static InputStreamSupplier bytes(byte[] bytes) {
        return new ByteArrayInputStreamSupplier(bytes);
    }

    public static InputStreamSupplier file(File file) {
        return new FileInputStreamSupplier(file);
    }

    public static InputStreamSupplier file(String file) {
        return new FileInputStreamSupplier(new File(file));
    }

    public static InputStreamSupplier url(URL url) {
        return new URLInputStreamSupplier(url);
    }

    public static InputStreamSupplier url(String url) {
        try {
            return new URLInputStreamSupplier(new URL(url));
        } catch (MalformedURLException e) {
            throw new MailIOException(e);
        }
    }

    public static InputStreamSupplier inputStream(InputStream inputStream) {
        return new SingleUseInputStreamSupplier(inputStream);
    }

    private InputStreamSuppliers() {
    }
}
