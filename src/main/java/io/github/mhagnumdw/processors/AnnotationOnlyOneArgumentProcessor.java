package io.github.mhagnumdw.processors;

import static java.util.Collections.singletonList;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import io.github.mhagnumdw.utils.TextBlockUtil;
import org.openrewrite.Cursor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.List;

/**
 * This abstract class provides a common tasks for processing annotations that contain an annotation with single SQL/HQL
 * string as their argument. It handles the extraction of the query, formatting it, and updating the annotation with
 * the formatted query.
 */
abstract class AnnotationOnlyOneArgumentProcessor implements AnnotationProcessor {

    AnnotationOnlyOneArgumentProcessor() {
    }

    @Override
    public final J.Annotation process(J.Annotation annotation, Cursor cursor, Dialect dialect, FormatConfig formatConfig) {
        if (annotation.getType() == null) {
            return annotation;
        }

        if (!getFQN().equals(annotation.getType().toString())) {
            return annotation;
        }

        List<Expression> args = annotation.getArguments();

        if (args == null || args.size() != 1) {
            return annotation;
        }

        Expression arg = args.get(0);

        if (!TextBlockUtil.isTextBlock(arg)) {
            return annotation;
        }

        J.Literal literal = (J.Literal) arg;
        String indentation = TextBlockUtil.getParentIndentation(cursor) + TextBlockUtil.getFileIndent(cursor);

        J.Literal newLiteral = TextBlockUtil.formatTextBlock(literal, indentation, dialect, formatConfig);

        if (newLiteral == null) {
            // nothing has changed
            return annotation;
        }

        return annotation.withArguments(singletonList(newLiteral));
    }

}
