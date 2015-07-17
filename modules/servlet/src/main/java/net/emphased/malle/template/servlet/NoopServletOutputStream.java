package net.emphased.malle.template.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

class NoopServletOutputStream extends ServletOutputStream {

    public static final NoopServletOutputStream INSTANCE = new NoopServletOutputStream();

    private NoopServletOutputStream() {
    }

    @Override
    public void print(String s) throws IOException {
    }

    @Override
    public void print(boolean b) throws IOException {
    }

    @Override
    public void print(char c) throws IOException {
    }

    @Override
    public void print(int i) throws IOException {
    }

    @Override
    public void print(long l) throws IOException {
    }

    @Override
    public void print(float f) throws IOException {
    }

    @Override
    public void print(double d) throws IOException {
    }

    @Override
    public void println() throws IOException {
    }

    @Override
    public void println(String s) throws IOException {
    }

    @Override
    public void println(boolean b) throws IOException {
    }

    @Override
    public void println(char c) throws IOException {
    }

    @Override
    public void println(int i) throws IOException {
    }

    @Override
    public void println(long l) throws IOException {
    }

    @Override
    public void println(float f) throws IOException {
    }

    @Override
    public void println(double d) throws IOException {
    }

    @Override
    public void write(byte[] b) throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }
}
