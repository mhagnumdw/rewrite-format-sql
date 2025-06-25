package io.github.mhagnumdw;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.style.TabsAndIndentsStyle;
import org.openrewrite.style.NamedStyles;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.Collections;

@SuppressWarnings("java:S2699")
class FormatSqlBlockRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(
            new FormatSqlBlockRecipe(
                "io/github/mhagnumdw/fake/holidays/HolidayRepository.java",
                "plsql",
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

    // There is a small spacing issue with aliases reported here https://github.com/vertical-blank/sql-formatter/issues/77
    @DocumentExample
    @Test
    void shouldFormatSimpleSqlBlock() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                    @HQL(\"""
                        select h.*, c.name as country_name from Holiday h inner join Country c on h.country_id = c.id where h.year = :year and h.name != 'xpto' order by h.name\""")
                    void select();
                }
                """,
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                    @HQL(\"""
                        select
                            h. *,
                            c .name as country_name
                        from
                            Holiday h
                            inner join Country c on h.country_id = c .id
                        where
                            h.year = :year
                            and h.name != 'xpto'
                        order by
                            h.name\""")
                    void select();
                }
                """
            )
        );
    }

    @Test
    void shouldFormatSqlBlockInHqlAnnotation() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                    public interface Other {

                        @HQL(\"""
                            WHERE
                                (
                                    :name IS NULL
                                    OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                                )
                                AND
                                (
                                    :details IS NULL
                                    OR LOWER(details) LIKE LOWER(CONCAT('%', :details, '%'))
                                )
                            AND (:date IS NULL OR date = :date)\""")
                        void select();

                    }

                }
                """,
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                    public interface Other {

                        @HQL(\"""
                            WHERE
                                (
                                    :name IS NULL
                                    OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                                )
                                AND (
                                    :details IS NULL
                                    OR LOWER(details) LIKE LOWER(CONCAT('%', :details, '%'))
                                )
                                AND (
                                    :date IS NULL
                                    OR date = :date
                                )\""")
                        void select();

                    }

                }
                """
            )
        );
    }

    @Test
    void shouldFormatSqlBlockWithTabs() {
        TabsAndIndentsStyle tabsStyle = IntelliJ.tabsAndIndents().withUseTabCharacter(true);

        NamedStyles tabsNamedStyle = new NamedStyles(
            randomId(),
            "tabs-style",
            "Tabs style for testing",
            "test",
            Collections.emptySet(),
            Collections.singletonList(tabsStyle)
        );

        rewriteRun(
            spec -> spec.recipe(
                new FormatSqlBlockRecipe(
                    "io/github/mhagnumdw/fake/holidays/HolidayRepository.java",
                    "plsql",
                    "\t", // Use tab for indentation sql block
                    null,
                    null
                )
            ).allSources(s ->
                // Informing OpenRewrite about the code style, as automatic style
                // detection doesn't seem to work in tests ?
                s.markers(tabsNamedStyle)
            ),
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                	public interface Other {

                		@HQL(\"""
                			WHERE
                				(
                					:name IS NULL
                					OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                				)
                				AND
                				(
                					:details IS NULL
                					OR LOWER(details) LIKE LOWER(CONCAT('%', :details, '%'))
                				)
                			AND (:date IS NULL OR date = :date)\""")
                		void select();

                	}

                }
                """,
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                	public interface Other {

                		@HQL(\"""
                			WHERE
                				(
                					:name IS NULL
                					OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                				)
                				AND (
                					:details IS NULL
                					OR LOWER(details) LIKE LOWER(CONCAT('%', :details, '%'))
                				)
                				AND (
                					:date IS NULL
                					OR date = :date
                				)\""")
                		void select();

                	}

                }
                """
            )
        );
    }

    @Test
    void shouldFormatSqlBlockInQueryAnnotation() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import jakarta.data.repository.Query;

                public interface HolidayRepository {

                    @Query(\"""
                        select    * from
                        Holiday where
                        year = :year order
                        by  name
                        \""")
                    void select();
                }
                """,
                """
                package io.github.mhagnumdw.fake.holidays;

                import jakarta.data.repository.Query;

                public interface HolidayRepository {

                    @Query(\"""
                        select
                            *
                        from
                            Holiday
                        where
                            year = :year
                        order by
                            name\""")
                    void select();
                }
                """
            )
        );
    }

    @Test
    void shouldFormatSqlBlockInSqlAnnotation() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.SQL;

                public interface HolidayRepository {

                    @SQL(\"""
                        select    * from
                        Holiday where
                        year = :year order
                        by  name
                        \""")
                    void select();
                }
                """,
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.SQL;

                public interface HolidayRepository {

                    @SQL(\"""
                        select
                            *
                        from
                            Holiday
                        where
                            year = :year
                        order by
                            name\""")
                    void select();
                }
                """
            )
        );
    }

    // Should only change TextBlock
    @Test
    void shouldNotChangeNonTextBlockStrings() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import jakarta.data.repository.Query;
                import org.hibernate.annotations.processing.HQL;
                import org.hibernate.annotations.processing.SQL;

                public interface HolidayRepository {

                    @Query("\\"\\"\\"select * from Holiday h\\"\\"\\"")
                    void select1();

                    @Query("select * from \\nHoliday h")
                    void select2();

                    @HQL("\\"\\"\\"select * from Holiday h\\"\\"\\"")
                    void select3();

                    @HQL("select * from \\nHoliday h")
                    void select4();

                    @SQL("\\"\\"\\"select * from Holiday h\\"\\"\\"")
                    void select5();

                    @SQL("select * from \\nHoliday h")
                    void select6();
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
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                    @HQL(
                            \"""
                        WHERE
                            (
                                :name IS NULL
                                OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                            )
                            AND
                            (
                                :details IS NULL
                                OR LOWER(details) LIKE LOWER(CONCAT('%', :details, '%'))
                            )
                        AND (:date IS NULL OR date = :date)\""")
                    void select();
                }
                """,
                """
                package io.github.mhagnumdw.fake.holidays;

                import org.hibernate.annotations.processing.HQL;

                public interface HolidayRepository {

                    @HQL(
                            \"""
                        WHERE
                            (
                                :name IS NULL
                                OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                            )
                            AND (
                                :details IS NULL
                                OR LOWER(details) LIKE LOWER(CONCAT('%', :details, '%'))
                            )
                            AND (
                                :date IS NULL
                                OR date = :date
                            )\""")
                    void select();
                }
                """
                )
            );
    }

    // Should not change anything, as this class's path was not specified for analysis
    @Test
    void shouldNotChangeUnrelatedClasses() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.city;

                import jakarta.data.repository.Query;
                import org.hibernate.annotations.processing.HQL;

                public interface CityRepository {

                    @Query(\"""
                            select    *  from      City  ORDER by     date\""")
                    void select1();

                    @HQL(\"""
                        from City c
                        order by
                             c.name
                             \""")
                    void select2();
                }
                """
                )
            );
    }

    @Test
    void shouldNotChangeUnsupportedAnnotations() {
        rewriteRun(
            java(
                """
                package io.github.mhagnumdw.fake.holidays;

                import io.github.mhagnumdw.Unknown;

                public interface HolidayRepository {

                    @Unknown(\"""
                        select * from
                        Holiday
                        where
                                       year = :year
                        order by name
                        \""")
                    void select1();

                    @Unknown("select *    from Holiday    where year = :year order   by name")
                    void select2();
                }
                """
            )
        );
    }

}
