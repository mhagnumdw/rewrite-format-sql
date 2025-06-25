package io.github.mhagnumdw;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.FindSourceFiles;
import org.openrewrite.Preconditions;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.search.UsesJavaVersion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * A recipe that formats SQL/HQL in Text Blocks within Java source files.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class FormatSqlBlockRecipe extends FormatSqlRecipeAbstract {

    private static final String DEFAULT_FILE_PATH = "**/*.java";

    @JsonCreator
    public FormatSqlBlockRecipe(@Nullable @JsonProperty("filePath") String filePath,
                                @Nullable @JsonProperty("sqlDialect") String sqlDialect,
                                @Nullable @JsonProperty("indent") String indent,
                                @Nullable @JsonProperty("maxColumnLength") Integer maxColumnLength,
                                @Nullable @JsonProperty("uppercase") Boolean uppercase) {
        super(filePath == null ? DEFAULT_FILE_PATH : filePath, sqlDialect, indent, maxColumnLength, uppercase);
    }

    @Override
    public String getDisplayName() {
        return "Format SQL Blocks in Java Code";
    }

    @Override
    public String getDescription() {
        return "Automatically formats embedded SQL code blocks within Java source files, improving readability and consistency.";
    }

    @Override
    TreeVisitor<?, ExecutionContext> getFormattingVisitor(Dialect dialect, FormatConfig formatConfig) {
        TreeVisitor<?, ExecutionContext> check = Preconditions.and(
            new FindSourceFiles(getFilePath()).getVisitor(),
            new UsesJavaVersion<>(13) // Text blocks were introduced as a preview feature in Java 13 and became a standard feature in Java 15
        );
        return Preconditions.check(check, new FormatSqlBlockVisitor(dialect, formatConfig));
    }
}
