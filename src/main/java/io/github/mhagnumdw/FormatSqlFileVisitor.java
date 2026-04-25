package io.github.mhagnumdw;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.text.PlainText;

import java.util.Objects;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

public class FormatSqlFileVisitor extends TreeVisitor<Tree, ExecutionContext> {

    private final Dialect dialect;
    private final FormatConfig formatConfig;

    public FormatSqlFileVisitor(Dialect dialect, FormatConfig formatConfig) {
        this.dialect = dialect;
        this.formatConfig = formatConfig;
    }

    @Override
    public @Nullable SourceFile visit(@Nullable Tree tree, ExecutionContext p) {
        SourceFile sourceFile = (SourceFile) Objects.requireNonNull(tree);

        if (!(sourceFile instanceof PlainText)) {
            return sourceFile;  // We only want pure text files
        }

        PlainText plainText = (PlainText) sourceFile;
        String originalText = plainText.getText();

        String sqlFormatted = SqlFormatter.of(dialect).format(originalText, formatConfig);

        if (originalText.equals(sqlFormatted)) {
            return plainText;
        }

        return plainText.withText(sqlFormatted);
    }

}
