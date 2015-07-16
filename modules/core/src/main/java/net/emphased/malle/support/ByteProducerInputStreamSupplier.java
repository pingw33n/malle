package net.emphased.malle.support;

import net.emphased.malle.InputStreamSupplier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ByteProducerInputStreamSupplier implements InputStreamSupplier {

    private final ByteProducer producer;

    public ByteProducerInputStreamSupplier(ByteProducer producer) {
        this.producer = producer;
    }

    @Override
    public InputStream get() throws IOException {
        final Path tempFile = Files.createTempFile("malle_", "");
        InputStream r;
        try {
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile.toFile()))) {
                producer.writeTo(os);
            }
            r = new FileInputStream(tempFile.toFile());
        } catch (RuntimeException outer) {
            try {
                Files.delete(tempFile);
            }  catch (IOException inner) {
                outer.addSuppressed(inner);
            }
            throw outer;
        }
        return new InputStreamWrapper(new BufferedInputStream(r)) {

            @Override
            public void close() throws IOException {
                IOException outer = null;
                try {
                    super.close();
                } catch (IOException e) {
                    outer = e;
                }
                try {
                    Files.delete(tempFile);
                } catch (IOException e) {
                    if (outer != null) {
                        outer.addSuppressed(e);
                    } else {
                        outer = e;
                    }
                }
                if (outer != null) {
                    throw outer;
                }
            }
        };
    }
}
