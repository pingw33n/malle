package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;
import net.emphased.malle.MailIOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static net.emphased.malle.util.Preconditions.checkNotNull;

class FileInputStreamSupplier implements InputStreamSupplier {

    private final File file;

    public FileInputStreamSupplier(File file) {
        this.file = checkNotNull(file);
    }

    public File getFile() {
        return file;
    }

    @Override
    public InputStream get() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new MailIOException(e);
        }
    }
}
