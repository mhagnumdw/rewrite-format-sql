package io.github.mhagnumdw.recipes;

import static org.openrewrite.test.SourceSpecs.other;
import static org.openrewrite.test.SourceSpecs.text;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@SuppressWarnings("java:S2699")
class FormatSqlFileTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(
            new FormatSqlFile(
                "file.sql",
                "plsql",
                null,
                null,
                null
            )
        );
    }

    // There is a small spacing issue with aliases reported at https://github.com/vertical-blank/sql-formatter/issues/77
    @DocumentExample
    @Test
    void shouldFormatSql() {
        rewriteRun(
            text(
                """
                select * from users where id = 1;
                --
                select d.name, c.name from City c inner join Department d on c.id = d.city_id;
                """,
                """
                select
                    *
                from
                    users
                where
                    id = 1;
                --
                select
                    d.name,
                    c .name
                from
                    City c
                    inner join Department d on c .id = d.city_id;
                """,
                spec -> spec.path("file.sql")
            )
        );
    }

    @Test
    void shouldNotChangeFormattedSql() {
        rewriteRun(
            text(
                """
                select
                    *
                from
                    users
                where
                    id = 1;
                """,
                spec -> spec.path("file.sql")
            )
        );
    }

    @Test
    void shouldNotChangeEmptyFile() {
        rewriteRun(
            text(
                "",
                spec -> spec.path("file.sql")
            )
        );
    }

    @Test
    void shouldFormatWithCustomOptions() {
        rewriteRun(
            spec -> spec.recipe(new FormatSqlFile(null, "plsql", "\t", null, false)),
            text(
                """
                select e.emp_id, e.name, d.dept_name from employees e join departments d on e.dept_id = d.dept_id where e.salary > 50000;
                """,
                """
                select
                	e.emp_id,
                	e.name,
                	d.dept_name
                from
                	employees e
                	join departments d on e.dept_id = d.dept_id
                where
                	e.salary > 50000;
                """,
                spec -> spec.path("test.sql")
            )
        );
    }

    // Should not change anything, as this file's path was not specified for processing
    @Test
    void shouldNotChangeOtherFiles() {
        rewriteRun(
            text(
                "select    * frOM    products ; ",
                spec -> spec.path("do-not-touch.sql")
            )
        );
    }

    // This test ensures that a non-PlainText file is not processed,
    // even if it has a .sql extension.
    @Test
    void shouldNotProcessBinaryFile() {
        rewriteRun(
            other(
                "some binary or unknown content",
                spec -> spec.path("file.sql")
            )
        );
    }

    @Test
    void shouldPreserveEndingNewline() {
        rewriteRun(
            // Single newline at end of file
            text(
                "select * from users;\n",
                "select\n    *\nfrom\n    users;\n",
                spec -> spec.path("file.sql").noTrim()
            ),
            // Multiple newlines at end of file
            text(
                "select * from users;\n\n\n\n",
                "select\n    *\nfrom\n    users;\n",
                spec -> spec.path("file.sql").noTrim()
            ),
            // No ending newline
            text(
                "select * from users;",
                "select\n    *\nfrom\n    users;",
                spec -> spec.path("file.sql").noTrim()
            ),
            // Windows line ending: \r\n at end of file
            text(
                "select * from users;\r\n",
                "select\n    *\nfrom\n    users;\r\n",
                spec -> spec.path("file.sql").noTrim()
            ),
            // Carriage return only (old Mac) at end of file
            text(
                "select * from users;\r",
                "select\n    *\nfrom\n    users;\r",
                spec -> spec.path("file.sql").noTrim()
            )
        );
    }
}
