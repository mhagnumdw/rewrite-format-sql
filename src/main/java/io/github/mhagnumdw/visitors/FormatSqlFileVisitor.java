package io.github.mhagnumdw.visitors;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import io.github.mhagnumdw.utils.NewlineType;
import io.github.mhagnumdw.utils.SourceUtils;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.text.PlainText;

import java.util.Objects;

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

        NewlineType endingNewline = SourceUtils.detectEndingNewline(originalText);

        String sqlFormatted = SqlFormatter.of(dialect).format(originalText, formatConfig);

        sqlFormatted = sqlFormatted + endingNewline.getValue();

        if (originalText.equals(sqlFormatted)) {
            return plainText;
        }

        return plainText.withText(sqlFormatted);
    }

}
