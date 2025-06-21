package io.github.mhagnumdw.processors;

import static org.openrewrite.Tree.randomId;

import org.openrewrite.Cursor;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.style.TabsAndIndentsStyle;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.style.Style;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

import java.util.Collections;
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
        JavaType type = annotation.getType();
        if (type == null) {
            return annotation;
        }

        if (!getFQN().equals(type.toString())) {
            return annotation;
        }

        List<Expression> args = annotation.getArguments();

        if (args == null || args.size() != 1) {
            return annotation;
        }

        Expression arg = args.get(0);

        if (!isTextBlock(arg)) {
            return annotation;
        }

        J.Literal literal = (Literal) arg;
        String sql = (String) literal.getValue();

        String sqlFormatted = SqlFormatter.of(dialect).format(sql, formatConfig);

        String indentation = getParentIndentation(cursor) + getFileIndent(cursor);

        // handle preceding indentation
        sqlFormatted = sqlFormatted.replace("\n", "\n" + indentation);

        // add first line
        sqlFormatted = "\n" + indentation + sqlFormatted;

        if (sqlFormatted.equals(sql)) {
            // nothing has changed
            return annotation;
        }

        J.Literal newLiteral = new J.Literal(randomId(), literal.getPrefix(), Markers.EMPTY, sqlFormatted,
            String.format("\"\"\"%s\"\"\"", sqlFormatted), null, JavaType.Primitive.String);

        return annotation.withArguments(Collections.singletonList(newLiteral));
    }

    // Retrieve the file indentation based on the style of the file
    private String getFileIndent(Cursor cursor) {
        JavaSourceFile sf = cursor.firstEnclosingOrThrow(JavaSourceFile.class);
        TabsAndIndentsStyle style = Style.from(TabsAndIndentsStyle.class, sf);
        if (style == null) {
            style = IntelliJ.tabsAndIndents();
        }

        boolean useTab = style.getUseTabCharacter();
        int tabSize = style.getTabSize();

        if (useTab) {
            return "\t";
        }
        return " ".repeat(tabSize);
    }

    // From: https://github.com/openrewrite/rewrite-migrate-java/blob/main/src/main/java/org/openrewrite/java/migrate/lang/UseTextBlocks.java
    private static boolean isTextBlock(Expression expr) {
        if (expr instanceof J.Literal) {
            J.Literal l = (J.Literal) expr;
            return TypeUtils.isString(l.getType()) &&
                   l.getValueSource() != null &&
                   l.getValueSource().startsWith("\"\"\"");
        }
        return false;
    }

    // Retrieve the indentation of the parent method declaration
    private static String getParentIndentation(Cursor cursor) {
        Cursor parentCursor = cursor.getParent();

        while (parentCursor != null) {
            Object parent = parentCursor.getValue();

            if (parent instanceof J.MethodDeclaration) {
                J.MethodDeclaration lstNode = (J.MethodDeclaration) parent;
                return lstNode.getPrefix().getIndent();
            }

            parentCursor = parentCursor.getParent();
        }

        return "";
    }

}
