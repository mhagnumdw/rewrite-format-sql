package io.github.mhagnumdw.recipes;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

// SonarQube doesn't recognize internal assertions in rewriteRun()
@SuppressWarnings("java:S2699")
class FormatSqlTextBlockByLanguageInjectionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(
            new FormatSqlTextBlockByLanguageInjection(
                "io/github/mhagnumdw/test/*.java",
                "sql",
                null,
                null,
                null
            )
        )
        .allSources(s -> s.markers(
            // https://docs.openrewrite.org/authoring-recipes/recipe-testing#specifying-java-versions
            javaVersion(13) // Text blocks were introduced as a preview feature in Java 13 and became a standard feature in Java 15
        ));
    }

    @DocumentExample
    @Test
    void shouldFormatFieldWithLanguageSqlComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    private static final String QUERY = \"""
                        select * from users u inner join orders o on u.id = o.user_id where u.active = true order by u.name\""";
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    private static final String QUERY = \"""
                        select
                            *
                        from
                            users u
                            inner join orders o on u.id = o.user_id
                        where
                            u.active = true
                        order by
                            u.name\""";
                }
                """
            )
        );
    }

    @Test
    void shouldFormatLocalVariableWithLanguageSqlComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    public void test() {
                        // language=sql
                        String query = \"""
                            select * from users where active = true\""";
                    }
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    public void test() {
                        // language=sql
                        String query = \"""
                            select
                                *
                            from
                                users
                            where
                                active = true\""";
                    }
                }
                """
            )
        );
    }

    @Test
    void shouldBeCaseInsensitive() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // LANGUAGE=SQL
                    private static final String Q1 = \"""
                        select * from users\""";
                    // Language=Sql
                    private static final String Q2 = \"""
                        select * from orders\""";
                    //language=sql
                    private static final String Q3 = \"""
                        select * from products\""";
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // LANGUAGE=SQL
                    private static final String Q1 = \"""
                        select
                            *
                        from
                            users\""";
                    // Language=Sql
                    private static final String Q2 = \"""
                        select
                            *
                        from
                            orders\""";
                    //language=sql
                    private static final String Q3 = \"""
                        select
                            *
                        from
                            products\""";
                }
                """
            )
        );
    }

    // Text Block without comment - does not change
    @Test
    void shouldNotFormatWithoutComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    private static final String QUERY = \"""
                        select * from users\""";
                }
                """
            )
        );
    }

    // Normal string with // language=sql - does not change
    @Test
    void shouldNotFormatNonTextBlockWithComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    private static final String QUERY = "select * from users";
                }
                """
            )
        );
    }

    // Because we only support `language=sql` comment for both sql and hql,
    // shouldn't do anything with `language=hql` comment
    @Test
    void shouldNotFormatWithLanguageHqlComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=hql
                    private static final String QUERY = \"""
                        select * from users\""";
                }
                """
            )
        );
    }

    @Test
    void shouldPreserveTextBlockIndentation() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    public class Inner {
                        // language=sql
                        private static final String QUERY = \"""
                            select * from users\""";
                    }
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    public class Inner {
                        // language=sql
                        private static final String QUERY = \"""
                            select
                                *
                            from
                                users\""";
                    }
                }
                """
            )
        );
    }

    // Block already formatted - no diff
    @Test
    void shouldNotChangeAlreadyFormatted() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    private static final String QUERY = \"""
                        select
                            *
                        from
                            users\""";
                }
                """
            )
        );
    }

    // Block with multiple variables in the same declaration
    @Test
    void shouldFormatMultipleVariablesInSameDeclaration() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    private static final String Q1 = \"""
                        select * from users\""",
                        Q2 = \"""
                        select * from orders\""",
                        Q3 = \"""
                        select * from products\""";
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    private static final String Q1 = \"""
                        select
                            *
                        from
                            users\""",
                        Q2 = \"""
                        select
                            *
                        from
                            orders\""",
                        Q3 = \"""
                        select
                            *
                        from
                            products\""";
                }
                """
            )
        );
    }

    // Opt-out: language=sql present but sql-format:off - does not change
    @Test
    void shouldNotFormatWhenNoFormatComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    // sql-format:off
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """
            )
        );
    }

    // Opt-out works regardless of comment order
    @Test
    void shouldNotFormatWhenNoFormatCommentBeforeLanguage() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // sql-format:off
                    // language=sql
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """
            )
        );
    }

    // Opt-out marker is case-insensitive
    @Test
    void shouldNotFormatWhenNoFormatCommentCaseInsensitive() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    // SQL-FORMAT:OFF
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """
            )
        );
    }

    // Opt-out marker must match exactly - extra spaces are not recognized, so the block is formatted
    @Test
    void shouldFormatWhenNoFormatCommentHasExtraSpaces() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    // sql-format : off
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    // sql-format : off
                    private static final String QUERY = \"""
                        select
                            *
                        from
                            users
                        where
                            active = true\""";
                }
                """
            )
        );
    }

    // sql-format:off without language=sql is irrelevant - still nothing to format
    @Test
    void shouldNotFormatNoFormatCommentWithoutLanguage() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // sql-format:off
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """
            )
        );
    }

    // A multiline block comment is not the opt-out marker, so the block is still formatted
    @Test
    void shouldFormatWithAdjacentMultilineComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    /* just a note, not a marker */
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    /* just a note, not a marker */
                    private static final String QUERY = \"""
                        select
                            *
                        from
                            users
                        where
                            active = true\""";
                }
                """
            )
        );
    }

    // A Javadoc comment is not a line comment, so it is ignored and the block is still formatted
    @Test
    void shouldFormatWithAdjacentJavadocComment() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    /** Javadoc note, not a marker */
                    private static final String QUERY = \"""
                        select * from users where active = true\""";
                }
                """,
                """
                package io.github.mhagnumdw.test;

                public class MyQuery {
                    // language=sql
                    /** Javadoc note, not a marker */
                    private static final String QUERY = \"""
                        select
                            *
                        from
                            users
                        where
                            active = true\""";
                }
                """
            )
        );
    }

    // Class outside the filePath - does not change
    @Test
    void shouldNotChangeUnrelatedClasses() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.other;

                public class OtherQuery {
                    // language=sql
                    private static final String QUERY = \"""
                        select    *  from      users  ORDER by     name\""";
                }
                """
            )
        );
    }

}
