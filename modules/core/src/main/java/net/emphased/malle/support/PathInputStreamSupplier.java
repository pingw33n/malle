package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;

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
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(path.toFile());
    }
}
