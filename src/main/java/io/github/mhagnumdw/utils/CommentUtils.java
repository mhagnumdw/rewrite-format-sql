package io.github.mhagnumdw.utils;

import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.TextComment;

import java.util.List;

/**
 * Utility class for analyzing line comments that control SQL formatting.
 */
public final class CommentUtils {

    /**
     * Opt-out marker. When present as a single-line comment near a SQL block, the recipe skips
     * formatting that block. Matched exactly (case-insensitive).
     */
    private static final String NO_FORMAT_MARKER = "sql-format:off";

    private CommentUtils() {
    }

    /**
     * Returns {@code true} if any of the given comments is the {@code sql-format:off} opt-out marker.
     *
     * @param comments the comments to inspect (may be {@code null})
     */
    public static boolean hasNoFormatComment(List<Comment> comments) {
        if (comments == null) {
            return false;
        }
        for (Comment comment : comments) {
            if (comment instanceof TextComment) {
                TextComment tc = (TextComment) comment;
                if (!tc.isMultiline() && NO_FORMAT_MARKER.equalsIgnoreCase(tc.getText().trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}
