package net.emphased.malle.template.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

class DummyHttpServletResponse implements HttpServletResponse {

    private final PrintWriter writer = new PrintWriter(NoopWriter.INSTANCE);

    @Override
    public void setCharacterEncoding(String characterEncoding) {
    }

    @Override
    public String getCharacterEncoding() {
        return "ISO-8859-1";
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return NoopServletOutputStream.INSTANCE;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        return this.writer;
    }

    @Override
    public void setContentLength(int contentLength) {
    }

    @Override
    public void setContentType(String contentType) {
    }

    @Override
    public String getContentType() {
        return "text/html;charset=UTF-8";
    }

    @Override
    public void setBufferSize(int bufferSize) {    }

    @Override
    public int getBufferSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void flushBuffer() {
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setLocale(Locale locale) {
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }


    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int status, String errorMessage) throws IOException {
    }

    @Override
    public void sendError(int status) throws IOException {
    }

    @Override
    public void sendRedirect(String url) throws IOException {
    }

    @Override
    public void setDateHeader(String name, long value) {
    }

    @Override
    public void addDateHeader(String name, long value) {
    }

    @Override
    public void setHeader(String name, String value) {
    }

    @Override
    public void addHeader(String name, String value) {
    }

    @Override
    public void setIntHeader(String name, int value) {
    }

    @Override
    public void addIntHeader(String name, int value) {
    }

    @Override
    public void setStatus(int status) {
    }

    @Override
    @Deprecated
    public void setStatus(int status, String errorMessage) {
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public void setContentLengthLong(long l) {
    }
}
