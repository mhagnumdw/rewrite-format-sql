package io.github.mhagnumdw;

import static org.openrewrite.java.tree.J.Literal;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TextComment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

public class FormatSqlTextBlockVisitor extends JavaIsoVisitor<ExecutionContext> {

    private static final Pattern LANGUAGE_SQL_PATTERN = Pattern.compile(
        "\\s*language\\s*=\\s*sql\\s*", Pattern.CASE_INSENSITIVE
    );

    private final Dialect dialect;
    private final FormatConfig formatConfig;

    FormatSqlTextBlockVisitor(Dialect dialect, FormatConfig formatConfig) {
        this.dialect = dialect;
        this.formatConfig = formatConfig;
    }

    @Override
    public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations varDecls, ExecutionContext ctx) {

        if (!hasLanguageSqlComment(varDecls)) {
            return varDecls;
        }

        List<J.VariableDeclarations.NamedVariable> variables = varDecls.getVariables();
        boolean changed = false;

        for (int i = 0; i < variables.size(); i++) {
            J.VariableDeclarations.NamedVariable var = variables.get(i);
            Expression initializer = var.getInitializer();

            if (initializer == null || !TextBlockUtil.isTextBlock(initializer)) {
                continue;
            }

            String indentation = getParentIndentation(varDecls) +
                               TextBlockUtil.getFileIndent(getCursor());

            Literal newLiteral = TextBlockUtil.formatTextBlock(
                (Literal) initializer, indentation, dialect, formatConfig);

            if (newLiteral != null) {
                var = var.withInitializer(newLiteral);
                List<J.VariableDeclarations.NamedVariable> newVariables = new ArrayList<>(variables);
                newVariables.set(i, var);
                variables = newVariables;
                changed = true;
            }
        }

        if (!changed) {
            return varDecls;
        }

        return varDecls.withVariables(variables);
    }

    private static boolean hasLanguageSqlComment(J.VariableDeclarations varDecls) {
        List<Comment> comments = varDecls.getPrefix().getComments();
        if (comments == null) {
            return false;
        }

        for (Comment comment : comments) {
            if (comment instanceof TextComment) {
                TextComment tc = (TextComment) comment;
                if (!tc.isMultiline() && LANGUAGE_SQL_PATTERN.matcher(tc.getText()).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getParentIndentation(J.VariableDeclarations varDecls) {
        return varDecls.getPrefix().getIndent();
    }
}
