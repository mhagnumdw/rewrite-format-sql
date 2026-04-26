package io.github.mhagnumdw.utils;

import static org.openrewrite.Tree.randomId;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.style.TabsAndIndentsStyle;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.style.Style;

/**
 * Utility class for formatting SQL in Java Text Blocks.
 * Provides shared methods used by both annotation processors and the Text Block visitor.
 */
public final class TextBlockUtil {

    private TextBlockUtil() {}

    /**
     * Checks if the given expression is a Java Text Block ("""...""").
     *
     * @param expr The expression to check
     * @return true if the expression is a text block, false otherwise
     * @see <a href="https://github.com/openrewrite/rewrite-migrate-java/blob/main/src/main/java/org/openrewrite/java/migrate/lang/UseTextBlocks.java">UseTextBlocks.java</a>
     */
    public static boolean isTextBlock(Expression expr) {
        if (expr instanceof J.Literal) {
            J.Literal l = (J.Literal) expr;
            return TypeUtils.isString(l.getType()) &&
                   l.getValueSource() != null &&
                   l.getValueSource().startsWith("\"\"\"");
        }
        return false;
    }

    /**
     * Formats SQL in a text block literal and returns the new literal,
     * or null if nothing changed.
     *
     * @param literal The text block literal containing SQL
     * @param indentation The indentation string to apply to formatted SQL
     * @param dialect The SQL dialect to use for formatting
     * @param formatConfig The format configuration
     * @return The new formatted literal, or null if nothing changed
     */
    public static  J.@Nullable Literal formatTextBlock(
            J.Literal literal,
            String indentation,
            Dialect dialect,
            FormatConfig formatConfig) {

        String sql = (String) literal.getValue();
        if (sql == null) {
            return null;
        }

        String sqlFormattedRaw = SqlFormatter.of(dialect).format(sql, formatConfig);

        if (sqlFormattedRaw.equals(sql)) {
            return null;
        }

        String sqlFormatted = sqlFormattedRaw.replace("\n", "\n" + indentation);
        sqlFormatted = "\n" + indentation + sqlFormatted;

        if (sqlFormatted.equals(literal.getValue())) {
            return null;
        }

        return new J.Literal(
            randomId(),
            literal.getPrefix(),
            Markers.EMPTY,
            sqlFormatted,
            String.format("\"\"\"%s\"\"\"", sqlFormatted),
            null,
            JavaType.Primitive.String
        );
    }

    /**
     * Retrieves the file indentation from the cursor context.
     *
     * @param cursor The cursor to extract indentation from
     * @return The indentation string (spaces or tab)
     */
    public static String getFileIndent(Cursor cursor) {
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
        return StringUtils.repeat(" ", tabSize);
    }

    /**
     * Retrieves the indentation of the parent node from the cursor context.
     *
     * @param cursor The cursor to extract indentation from
     * @return The indentation string (spaces or tab)
     */
    public static String getParentIndentation(Cursor cursor) {
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
