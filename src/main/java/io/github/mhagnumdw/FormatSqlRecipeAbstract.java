package io.github.mhagnumdw;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Abstract base class for SQL formatting recipes.
 * Provides common configuration options and methods for SQL formatting.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
abstract class FormatSqlRecipeAbstract extends Recipe {

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
            "If multiple patterns are supplied any of the patterns matching will be interpreted as a match.",
        required = false,
        example = "**/*Repository.java;**/*.sql")
    @Nullable
    private final String filePath;

    @Option(
        displayName = "SQL Dialect",
        description = "The SQL dialect to use for formatting. E.g., sql (StandardSql), mysql, postgresql, db2, plsql (Oracle PL/SQL), n1ql (Couchbase N1QL), redshift, spark, tsql (SQL Server Transact-SQL). Defaults to " + DEFAULT_DIALECT_ALIAS + ".",
        required = false,
        valid = {"sql", "mysql", "postgresql", "db2", "plsql", "n1ql", "redshift", "spark", "tsql"},
        example = "plsql")
    @Nullable
    private final String sqlDialect;

    @Option(
        displayName = "Indent",
        description = "The indentation string to use. Defaults to " + DEFAULT_INDENT_SIZE + " spaces.",
        required = false,
        example = "  ")
    @Nullable
    private final String indent;

    @Option(
        displayName = "Max Column Length",
        description = "The maximum length of a line before it's wrapped. Defaults to " + DEFAULT_MAX_COLUMN_LENGTH + ".",
        required = false,
        example = "100")
    @Nullable
    private final Integer maxColumnLength;

    @Option(
        displayName = "Uppercase",
        description = "Whether to convert keywords to uppercase (not safe to use when SQL dialect has case-sensitive identifiers). Defaults to " + DEFAULT_UPPERCASE + ".",
        required = false,
        example = "true")
    @Nullable
    private final Boolean uppercase;

    FormatSqlRecipeAbstract(@Nullable @JsonProperty("filePath") String filePath,
                            @Nullable @JsonProperty("sqlDialect") String sqlDialect,
                            @Nullable @JsonProperty("indent") String indent,
                            @Nullable @JsonProperty("maxColumnLength") Integer maxColumnLength,
                            @Nullable @JsonProperty("uppercase") Boolean uppercase) {
        this.filePath = filePath;
        this.sqlDialect = sqlDialect;
        this.indent = indent;
        this.maxColumnLength = maxColumnLength;
        this.uppercase = uppercase;
    }

    private FormatConfig getFormatConfig() {
        return FormatConfig.builder()
            .indent(indent == null ? DEFAULT_INDENT : indent)
            .maxColumnLength(maxColumnLength == null ? DEFAULT_MAX_COLUMN_LENGTH : maxColumnLength)
            .uppercase(uppercase == null ? DEFAULT_UPPERCASE : uppercase)
            .build();
    }

    private Dialect getDialect() {
        return Dialect.nameOf(sqlDialect).orElse(DEFAULT_DIALECT);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return getFormattingVisitor(getDialect(), getFormatConfig());
    }

    /**
     * Returns a visitor that formats SQL code based on the specified dialect and format configuration.
     */
    abstract TreeVisitor<?, ExecutionContext> getFormattingVisitor(Dialect dialect, FormatConfig formatConfig);

}
