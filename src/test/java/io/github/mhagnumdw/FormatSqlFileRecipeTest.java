package io.github.mhagnumdw;

import static org.openrewrite.test.SourceSpecs.other;
import static org.openrewrite.test.SourceSpecs.text;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@SuppressWarnings("java:S2699")
class FormatSqlFileRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(
            new FormatSqlFileRecipe(
                "file.sql",
                "plsql",
                null,
                null,
                null
            )
        );
    }

    // There is a small spacing issue with aliases reported here https://github.com/vertical-blank/sql-formatter/issues/77
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
            spec -> spec.recipe(new FormatSqlFileRecipe(null, "plsql", "\t", null, false)),
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

    // Should not change anything, as this file's path was not specified for analysis
    @Test
    void shouldNotChangeOtherFiles() {
        rewriteRun(
            text(
                "select    * frOM    products ; ",
                spec -> spec.path("do-not-touch.sql")
            )
        );
    }

    // This test ensures that a file that is not a PlainText file is not processed
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
}
