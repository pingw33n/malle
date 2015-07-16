package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;
import net.emphased.malle.MailIOException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class InputStreamSuppliers {

    public static InputStreamSupplier bytes(byte[] bytes, int offset, int size) {
        return new ByteArrayInputStreamSupplier(bytes, offset, size);
    }

    public static InputStreamSupplier bytes(byte[] bytes) {
        return new ByteArrayInputStreamSupplier(bytes);
    }

    public static InputStreamSupplier file(Path path) {
        return new PathInputStreamSupplier(path);
    }

    public static InputStreamSupplier file(String path) {
        return file(Paths.get(path));
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

    public static InputStreamSupplier resource(String name) {
        return resource(name, InputStreamSupplier.class.getClassLoader());
    }

    public static InputStreamSupplier resource(String name, Class<?> clazz) {
        URL url = clazz.getResource(name);
        if (url == null) {
            throw new MailIOException("Couldn't find the specified resource: " + name);
        }
        return url(url);
    }

    public static InputStreamSupplier resource(String name, ClassLoader classLoader) {
        URL url = classLoader.getResource(name);
        if (url == null) {
            throw new MailIOException("Couldn't find the specified resource: " + name);
        }
        return url(url);
    }
}
