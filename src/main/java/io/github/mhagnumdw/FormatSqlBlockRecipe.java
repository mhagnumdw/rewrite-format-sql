package io.github.mhagnumdw;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.FindSourceFiles;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
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
public class FormatSqlBlockRecipe extends Recipe {

    private static final String DEFAULT_CLASS_FILE_PATH = "**/*.java";

    private static final Dialect DEFAULT_DIALECT = Dialect.StandardSql;
    private static final String DEFAULT_DIALECT_ALIAS = "sql"; // according DEFAULT_DIALECT
    private static final String DEFAULT_INDENT = "    ";
    private static final int DEFAULT_INDENT_SIZE = 4; // according DEFAULT_INDENT
    private static final int DEFAULT_MAX_COLUMN_LENGTH = 120;
    private static final boolean DEFAULT_UPPERCASE = false;

    @Option(
        displayName = "File pattern",
        description = "A glob expression representing a file path to search for (relative to the project root). Blank/null matches all." +
                        "Multiple patterns may be specified, separated by a semicolon `;`. " +
                        "If multiple patterns are supplied any of the patterns matching will be interpreted as a match. " +
                        "Defaults to " + DEFAULT_CLASS_FILE_PATH + ".",
        required = false,
        example = "src/main/java/com/mycompany/Foo.java")
    @Nullable
    String classFilePath;

    @Option(
        displayName = "SQL Dialect",
        description = "The SQL dialect to use for formatting. E.g., sql (StandardSql), mysql, postgresql, db2, plsql (Oracle PL/SQL), n1ql (Couchbase N1QL), redshift, spark, tsql (SQL Server Transact-SQL). Defaults to " + DEFAULT_DIALECT_ALIAS + ".",
        required = false,
        valid = {"sql", "mysql", "postgresql", "db2", "plsql", "n1ql", "redshift", "spark", "tsql"},
        example = "plsql")
    @Nullable
    String sqlDialect;

    @Option(
        displayName = "Indent",
        description = "The indentation string to use. Defaults to " + DEFAULT_INDENT_SIZE + " spaces.",
        required = false,
        example = "  ")
    @Nullable
    String indent;

    @Option(
        displayName = "Max Column Length",
        description = "The maximum length of a line before it's wrapped. Defaults to " + DEFAULT_MAX_COLUMN_LENGTH + ".",
        required = false,
        example = "100")
    @Nullable
    Integer maxColumnLength;

    @Option(
        displayName = "Uppercase",
        description = "Whether to convert keywords to uppercase (not safe to use when SQL dialect has case-sensitive identifiers). Defaults to " + DEFAULT_UPPERCASE + ".",
        required = false,
        example = "true")
    @Nullable
    Boolean uppercase;

    @JsonCreator
    public FormatSqlBlockRecipe(@Nullable @JsonProperty("classFilePath") String classFilePath,
                                @Nullable @JsonProperty("sqlDialect") String sqlDialect,
                                @Nullable @JsonProperty("indent") String indent,
                                @Nullable @JsonProperty("maxColumnLength") Integer maxColumnLength,
                                @Nullable @JsonProperty("uppercase") Boolean uppercase) {
        this.classFilePath = classFilePath;
        this.sqlDialect = sqlDialect;
        this.indent = indent;
        this.maxColumnLength = maxColumnLength;
        this.uppercase = uppercase;
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
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        Dialect dialect = Dialect.nameOf(sqlDialect).orElse(DEFAULT_DIALECT);
        FormatConfig formatConfig = FormatConfig.builder()
            .indent(indent == null ? DEFAULT_INDENT : indent)
            .maxColumnLength(maxColumnLength == null ? DEFAULT_MAX_COLUMN_LENGTH : maxColumnLength)
            .uppercase(uppercase == null ? DEFAULT_UPPERCASE : uppercase)
            .build();
        TreeVisitor<?, ExecutionContext> check = Preconditions.and(
            new FindSourceFiles(classFilePath == null ? DEFAULT_CLASS_FILE_PATH : classFilePath).getVisitor(),
            new UsesJavaVersion<>(13) // Text blocks were introduced as a preview feature in Java 13 and became a standard feature in Java 15
        );
        return Preconditions.check(check, new FormatSqlBlockVisitor(dialect, formatConfig));
    }
}
