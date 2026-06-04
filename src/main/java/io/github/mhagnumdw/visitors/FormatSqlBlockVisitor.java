package io.github.mhagnumdw.visitors;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import io.github.mhagnumdw.processors.Annotations;
import io.github.mhagnumdw.utils.CommentUtils;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public class FormatSqlBlockVisitor extends JavaIsoVisitor<ExecutionContext> {

    private final Dialect dialect;
    private final FormatConfig formatConfig;

    public FormatSqlBlockVisitor(Dialect dialect, FormatConfig formatConfig) {
        this.dialect = dialect;
        this.formatConfig = formatConfig;
    }

    @Override
    public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext context) {
        JavaType type = annotation.getType();

        if (type == null) {
            return annotation;
        }

        if (hasNoFormatComment(annotation, getCursor())) {
            return annotation;
        }

        String annotationFQN = type.toString();

        return Annotations.getProcessor(annotationFQN)
            .map(p -> p.process(annotation, getCursor(), dialect, formatConfig))
            .orElse(annotation);
    }

    /**
     * Checks for the {@code sql-format:off} opt-out marker. The comment may sit either directly on
     * the annotation or on the enclosing declaration (method, field, class), e.g.:
     *
     * <pre>{@code
     * // sql-format:off
     * @Query("""...""")
     * void select();
     * }</pre>
     */
    private static boolean hasNoFormatComment(J.Annotation annotation, Cursor cursor) {
        if (CommentUtils.hasNoFormatComment(annotation.getPrefix().getComments())) {
            return true;
        }
        Object parent = cursor.getParentTreeCursor().getValue();
        if (parent instanceof J) {
            return CommentUtils.hasNoFormatComment(((J) parent).getPrefix().getComments());
        }
        return false;
    }

}
