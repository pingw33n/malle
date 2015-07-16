package net.emphased.malle.support;

import java.io.IOException;
import java.io.OutputStream;

public interface ByteProducer {

    void writeTo(OutputStream os) throws IOException;
}
