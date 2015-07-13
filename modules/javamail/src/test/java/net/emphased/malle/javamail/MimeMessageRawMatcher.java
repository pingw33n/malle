package net.emphased.malle.javamail;

import net.emphased.malle.Mail;

import javax.mail.MessagingException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeMessageRawMatcher {

    public static void assertMatch(Reader expected, Reader actual) throws IOException {
        int line = 0;
        List<String> vars = new ArrayList<String>();
        while (true) {
            line++;
            Line eline = readLine(expected);
            Line aline = readLine(actual);

            switch (aline.getTerminator()) {
                case CRLF:
                case EOF:
                    break;
                default:
                    throw new AssertionError("Line " + line + " terminator mismatch: CRLF expected but " + aline.getTerminator() + " found");
            }

            String elineStr = replace(eline.toString(), vars);
            Pattern epattern = toPattern(elineStr);

            Matcher m = epattern.matcher(aline.toString());
            if (!m.matches()) {
                throw new AssertionError("Line " + line + " mismatch: '" + elineStr + "' expected but '" + aline + " found");
            }

            for (int i = 0; i < m.groupCount(); i++) {
                vars.add(m.group(i + 1));
            }

            if (aline.getTerminator() == Line.Terminator.EOF) {
                break;
            }
        }
    }

    public static void assertMatch(String expectedResource, Mail actual) throws IOException, MessagingException {
        InputStream is = MimeMessageRawMatcher.class.getClassLoader().getResourceAsStream(expectedResource);
        if (is == null) {
            throw new IOException("Couldn't find resource: " + expectedResource);
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            actual.writeTo(baos);
            {
                FileOutputStream os = new FileOutputStream("/tmp/test2.eml");
                os.write(baos.toByteArray());
                os.close();
            }
            assertMatch(new BufferedReader(new InputStreamReader(is, "ISO-8859-1")),
                    new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), "ISO-8859-1")));
        } finally {
            is.close();
        }
    }

    private static Pattern toPattern(String s) {
        StringBuilder r = new StringBuilder(s.length() + 100);

        int pos = 0;
        while (true) {
            int rePos = s.indexOf('~', pos);
            if (rePos == -1) {
                break;
            }
            r.append(Pattern.quote(s.substring(pos, rePos)));
            pos = rePos + 1;
            rePos = s.indexOf('~', pos);
            if (rePos == -1) {
                rePos = s.length();
            }
            r.append(Pattern.compile(s.substring(pos, rePos)));
            pos = Math.min(s.length(), rePos + 1);
        }
        r.append(Pattern.quote(s.substring(pos)));

        return Pattern.compile(r.toString());
    }

    private static String replace(String s, List<String> vars) {
        for (int i = 0; i < vars.size(); i++) {
            s = s.replace("${" + i + "}", vars.get(i));
        }
        return s;
    }

    private static class Line {
        enum Terminator {
            CR, LF, CRLF, EOF
        }

        private final String content;
        private final Terminator terminator;

        public Line(CharSequence content, Terminator terminator) {
            this.content = content.toString();
            this.terminator = terminator;
        }

        public Terminator getTerminator() {
            return terminator;
        }

        @Override
        public String toString() {
            return content;
        }
    }

    private static Line readLine(Reader reader) throws IOException {
        StringBuilder r = new StringBuilder(1024);
        while (true) {
            Line.Terminator t = null;
            int c = reader.read();
            switch (c) {
                case -1:
                    return new Line(r, Line.Terminator.EOF);
                case '\r':
                    reader.mark(1);
                    c = reader.read();
                    switch (c) {
                        case -1:
                            reader.reset();
                            return new Line(r, Line.Terminator.CR);
                        case '\n':
                            t = Line.Terminator.CRLF;
                            break;
                        default:
                            reader.reset();
                            t = Line.Terminator.LF;
                    }
                    break;
                case '\n':
                    t = Line.Terminator.LF;
                default:
                    r.append((char) c);
            }

            if (t != null) {
                reader.mark(1);
                c = reader.read();
                if (c != '\t') {
                    reader.reset();
                    return new Line(r, t);
                }
                do {
                    reader.mark(1);
                    c = reader.read();
                } while (c == '\t');
                reader.reset();
            }
        }
    }

    private MimeMessageRawMatcher() {}
}
