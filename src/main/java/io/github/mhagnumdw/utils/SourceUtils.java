package io.github.mhagnumdw.utils;

/**
 * Utility class for source code analysis.
 */
public final class SourceUtils {

    private SourceUtils() {
    }

    /**
     * Detects the line ending type used in the given text.
     *
     * @param text the text to analyze
     * @return the detected newline type
     */
    public static NewlineType detectEndingNewline(String text) {
        int length = text.length();

        if (length == 0) {
            return NewlineType.NONE;
        }

        char last = text.charAt(length - 1);

        if (last == '\n') {
            if (length > 1 && text.charAt(length - 2) == '\r') {
                return NewlineType.CRLF;
            }
            return NewlineType.LF;
        }

        if (last == '\r') {
            return NewlineType.CR;
        }

        return NewlineType.NONE;
    }

}
