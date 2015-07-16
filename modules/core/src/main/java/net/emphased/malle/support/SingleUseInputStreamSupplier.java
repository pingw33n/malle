package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;

import java.io.InputStream;

import static net.emphased.malle.util.Preconditions.checkNotNull;

class SingleUseInputStreamSupplier implements InputStreamSupplier {

    private final InputStream inputStream;
    private boolean used;

    public SingleUseInputStreamSupplier(InputStream inputStream) {
        this.inputStream = checkNotNull(inputStream);
    }

    public boolean isUsed() {
        return used;
    }

    public InputStream getInputStreamSafe() {
        return inputStream;
    }

    @Override
    public InputStream getInputStream() {
        if (used) {
            throw new IllegalStateException("This InputStreamSupplier implementation is single use only");
        }
        used = true;
        return inputStream;
    }
}
