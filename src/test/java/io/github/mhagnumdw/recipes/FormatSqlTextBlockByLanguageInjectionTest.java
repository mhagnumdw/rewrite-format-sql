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

    // Text Block without comment — does not change
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

    // Normal string with // language=sql — does not change
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

    // Block already formatted — no diff
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

    // Class outside the filePath — does not change
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
