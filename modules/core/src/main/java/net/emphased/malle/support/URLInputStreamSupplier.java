package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static net.emphased.malle.util.Preconditions.checkNotNull;

class URLInputStreamSupplier implements InputStreamSupplier {

    private final URL url;

    public URLInputStreamSupplier(URL url) {
        this.url = checkNotNull(url);
    }

    @Override
    public InputStream get() throws IOException {
        return url.openStream();
    }
}
