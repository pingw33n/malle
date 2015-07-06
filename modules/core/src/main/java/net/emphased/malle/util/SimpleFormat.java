package net.emphased.malle.util;

import static net.emphased.malle.util.Preconditions.checkArgument;

public final class SimpleFormat {

    private static final String PLACEHOLDER = "{}";
    private static final char PLACEHOLDER_FIRST_CHAR = PLACEHOLDER.charAt(0);
    private static final String PLACEHOLDER_FIRST_CHAR_AS_STR = String.valueOf(PLACEHOLDER_FIRST_CHAR);
    private static final String ESCAPED_PLACEHOLDER_FIRST_CHAR = "" + PLACEHOLDER_FIRST_CHAR + PLACEHOLDER_FIRST_CHAR;

    public static String format(String s, Object... args) {
        int phIdx = s.indexOf(PLACEHOLDER_FIRST_CHAR);
        if (phIdx == -1) {
            return s;
        }
        StringBuilder r = new StringBuilder(s.length() + args.length * 16);
        int lastPhIdx = 0;
        int argIdx = 0;
        while (phIdx != -1) {
            r.append(s, lastPhIdx, phIdx);
            boolean isLast = phIdx == s.length() - 1;
            if (!isLast) {
                if (s.charAt(phIdx + 1) == PLACEHOLDER_FIRST_CHAR) {
                    // Escaped.
                    lastPhIdx = phIdx + 1;
                    phIdx += 2;
                } else if (isStringAt(s, PLACEHOLDER, phIdx)) {
                    // Placeholder matched.
                    checkArgument(argIdx < args.length, "Found more placeholders than arguments");
                    r.append(String.valueOf(args[argIdx]));
                    argIdx++;
                    lastPhIdx = phIdx + PLACEHOLDER.length();
                    phIdx += PLACEHOLDER.length();
                } else {
                    lastPhIdx = phIdx;
                    phIdx++;
                }
                phIdx = s.indexOf(PLACEHOLDER_FIRST_CHAR, phIdx);
            } else {
                lastPhIdx = phIdx;
                phIdx = -1;
            }
        }
        r.append(s, lastPhIdx, s.length());
        return r.toString();
    }

    public static String escape(String s) {
        return s.replace(PLACEHOLDER_FIRST_CHAR_AS_STR, ESCAPED_PLACEHOLDER_FIRST_CHAR);
    }

    public static String unescape(String s) {
        return s.replace(ESCAPED_PLACEHOLDER_FIRST_CHAR, PLACEHOLDER_FIRST_CHAR_AS_STR);
    }

    private static boolean isStringAt(String haystack, String needle, int idx) {
        if (haystack.length() - idx < needle.length()) {
            return false;
        }
        for (int hi = idx, ni = 0; ni < needle.length(); hi++, ni++) {
            if (haystack.charAt(hi) != needle.charAt(ni)) {
                return false;
            }
        }
        return true;
    }

    private SimpleFormat() {
    }
}
