package io.github.mhagnumdw;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.FindSourceFiles;
import org.openrewrite.Preconditions;
import org.openrewrite.TreeVisitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * A recipe that formats SQL text files.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class FormatSqlFileRecipe extends FormatSqlRecipeAbstract {

    private static final String DEFAULT_FILE_PATH = "**/*.sql";

    @JsonCreator
    public FormatSqlFileRecipe(@Nullable @JsonProperty("filePath") String filePath,
                               @Nullable @JsonProperty("sqlDialect") String sqlDialect,
                               @Nullable @JsonProperty("indent") String indent,
                               @Nullable @JsonProperty("maxColumnLength") Integer maxColumnLength,
                               @Nullable @JsonProperty("uppercase") Boolean uppercase) {
        super(filePath == null ? DEFAULT_FILE_PATH : filePath, sqlDialect, indent, maxColumnLength, uppercase);
    }

    @Override
    public String getDisplayName() {
        return "Format SQL files";
    }

    @Override
    public String getDescription() {
        return "Automatically formats SQL files, improving readability and consistency.";
    }

    @Override
    TreeVisitor<?, ExecutionContext> getFormattingVisitor(Dialect dialect, FormatConfig formatConfig) {
        FindSourceFiles check = new FindSourceFiles(getFilePath());
        return Preconditions.check(check, new FormatSqlFileVisitor(dialect, formatConfig));
    }
}
