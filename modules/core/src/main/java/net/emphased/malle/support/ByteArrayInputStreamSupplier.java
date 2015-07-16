package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static net.emphased.malle.util.Preconditions.checkArgument;
import static net.emphased.malle.util.Preconditions.checkNotNull;

class ByteArrayInputStreamSupplier implements InputStreamSupplier {

    private final byte[] bytes;
    private final int offset;
    private final int size;

    public ByteArrayInputStreamSupplier(byte[] bytes, int offset, int size) {
        this.bytes = checkNotNull(bytes);
        checkArgument(offset >= 0);
        checkArgument(offset + size <= bytes.length);
        this.offset = offset;
        checkArgument(size >= 0);
        this.size = size;
    }

    public ByteArrayInputStreamSupplier(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes, offset, size);
    }
}
