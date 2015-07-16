package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;
import net.emphased.malle.MailIOException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

import static net.emphased.malle.util.Preconditions.checkNotNull;

class PathInputStreamSupplier implements InputStreamSupplier {

    private final Path path;

    public PathInputStreamSupplier(Path path) {
        this.path = checkNotNull(path);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public InputStream get() {
        try {
            return new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new MailIOException(e);
        }
    }
}
