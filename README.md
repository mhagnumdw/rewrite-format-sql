# rewrite-format-sql <!-- omit in toc -->

[![ci](https://github.com/mhagnumdw/rewrite-format-sql/actions/workflows/ci.yml/badge.svg)](https://github.com/mhagnumdw/rewrite-format-sql/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/mhagnumdw/rewrite-format-sql/graph/badge.svg?token=88BB3MW6QA)](https://codecov.io/gh/mhagnumdw/rewrite-format-sql)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mhagnumdw_rewrite-format-sql&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mhagnumdw_rewrite-format-sql)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.mhagnumdw/rewrite-format-sql)](https://central.sonatype.com/artifact/io.github.mhagnumdw/rewrite-format-sql)

A set of [OpenRewrite](https://docs.openrewrite.org/) recipes for formatting SQL/HQL code.

- [Recipes](#recipes)
  - [FormatSqlBlockRecipe](#formatsqlblockrecipe)
  - [FormatSqlFileRecipe](#formatsqlfilerecipe)
- [Configurable Options](#configurable-options)
- [Examples](#examples)
  - [FormatSqlBlockRecipe Example](#formatsqlblockrecipe-example)
  - [FormatSqlFileRecipe Example](#formatsqlfilerecipe-example)
- [Usage](#usage)
  - [Configuring in `pom.xml`](#configuring-in-pomxml)
  - [Without adding anything to the project](#without-adding-anything-to-the-project)
- [For Developers](#for-developers)

## Recipes

Below is a detailed description of each Recipe.

### FormatSqlBlockRecipe

The `io.github.mhagnumdw.FormatSqlBlockRecipe` recipe automatically formats SQL or HQL embedded in [Text Blocks](https://docs.oracle.com/en/java/javase/13/text_blocks/index.html) present in the following annotations:

- `org.hibernate.annotations.processing.HQL`
- `org.hibernate.annotations.processing.SQL`
- `jakarta.data.repository.Query`

> Future enhancements may allow configuration of custom annotations. Please open an issue.

### FormatSqlFileRecipe

The `io.github.mhagnumdw.FormatSqlFileRecipe` recipe automatically formats the content of SQL files.

## Configurable Options

The following options are applicable to both `FormatSqlBlockRecipe` and `FormatSqlFileRecipe`:

| Type    | Name              | Description                                                                                                                                                                                           | Example  | Default Value |
| :------ | :---------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  | :------- | :------------------------- |
| String  | `filePath`        | Optional. The path to the files that the Recipe should process. Accepts a glob expression; multiple patterns can be specified, separated by a semicolon `;`. If omitted, processes all matching files. | `**/*DAO.java` <br> `**/*.sql` | FormatSqlBlockRecipe: `**/*.java` <br> FormatSqlFileRecipe: `**/*.sql` |
| String  | `sqlDialect`      | Optional. The SQL dialect to be used for formatting. Valid options: `sql` (StandardSql), `mysql`, `postgresql`, `db2`, `plsql` (Oracle PL/SQL), `n1ql` (Couchbase N1QL), `redshift`, `spark`, `tsql` (SQL Server Transact-SQL). Details [here](https://github.com/vertical-blank/sql-formatter). | `plsql`  | `sql` |
| String  | `indent`          | Optional. The string to be used for indentation.                                                                                                                                                     | `"  "` for 2 spaces <br> `"\t"` for a tab | 4 spaces `"    "` |
| Integer | `maxColumnLength` | Optional. The maximum length of a line before the formatter tries to break it.                                                                                                              | `100`    | `120` |
| Boolean | `uppercase`       | Optional. Whether to convert SQL keywords to uppercase (not safe to use when the SQL dialect has case-sensitive identifiers).                                        | `true`   | `false` |

## Examples

### FormatSqlBlockRecipe Example

Before

```java
package com.mycompany;

import jakarta.data.repository.Query;

public interface HolidayRepository {
    @Query("""
        select h.*, c.name as country_name from Holiday h inner join Country c on h.country_id = c.id where h.year = :year and h.name != 'Christmas' order by h.name""")
    void findByYear(int year);
}
```

After

```java
package com.mycompany;

import jakarta.data.repository.Query;

public interface HolidayRepository {
    @Query("""
        select
            h. *,
            c .name as country_name
        from
            Holiday h
            inner join Country c on h.country_id = c .id
        where
            h.year = :year
            and h.name != 'Christmas'
        order by
            h.name""")
    void findByYear(int year);
}
```

### FormatSqlFileRecipe Example

Consider the following `example.sql` file:

```sql
select * from users where id = 1;
--
select d.name, c.name from City c inner join Department d on c.id = d.city_id;
```

After running the recipe, the `example.sql` file will be:

```sql
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
```

> There is an open issue regarding the space in some aliases: <https://github.com/vertical-blank/sql-formatter/issues/77>

## Usage

The usage modes are described below. It is possible to apply one or both Recipes.

### Configuring in `pom.xml`

If you have a project and will run the recipe regularly, this is the recommended way.

Inside the plugins section, add:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.11.0</version>
    <configuration>
        <activeRecipes>
            <!-- Add the recipes you want to use here -->
            <recipe>io.github.mhagnumdw.FormatSqlBlockRecipe</recipe>
            <recipe>io.github.mhagnumdw.FormatSqlFileRecipe</recipe>
        </activeRecipes>
        <failOnDryRunResults>false</failOnDryRunResults>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>io.github.mhagnumdw</groupId>
            <artifactId>rewrite-format-sql</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```

Then run:

```bash
./mvnw rewrite:run
```

To customize the recipe configuration, you need to have a `rewrite.yml` file in the project root. Example:

```yml
---
type: specs.openrewrite.org/v1beta/recipe
name: io.github.mhagnumdw.FormatSqlCustomConfig
recipeList:
  # Add the Recipes you want to use here
  - io.github.mhagnumdw.FormatSqlBlockRecipe:
      sqlDialect: "plsql"
  - io.github.mhagnumdw.FormatSqlFileRecipe:
      sqlDialect: "mysql"
```

> - This file is a way to create your own custom recipe from other recipes.
> - The `name` attribute is arbitrary, but it's good if it relates to its purpose. It's the name of the custom recipe.
> - The `rewrite.yml` file should be versioned.
> - For more details about `rewrite.yml`, see [here](https://docs.openrewrite.org/reference/yaml-format-reference).

And change the `<recipe>` tag in `pom.xml` to:

```xml
<recipe>io.github.mhagnumdw.FormatSqlCustomConfig</recipe>
```

> As in this example the `FormatSqlCustomConfig` recipe includes both `FormatSqlBlockRecipe` and `FormatSqlFileRecipe` recipes, in `pom.xml` it is only necessary to define the `FormatSqlCustomConfig` recipe.

Then run:

```bash
./mvnw rewrite:run
```

For more details on configuring OpenRewrite with Maven, see [here](https://docs.openrewrite.org/reference/rewrite-maven-plugin).

### Without adding anything to the project

This mode is indicated if your intention is to run the recipe only once.

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.FormatSqlBlockRecipe,io.github.mhagnumdw.FormatSqlFileRecipe \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0.0
```

To customize the recipe configuration, you need to have the `rewrite.yml` file in the project root, **as in the previous example**.

Then run:

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.FormatSqlCustomConfig \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0.0
```

> - `io.github.mhagnumdw.FormatSqlCustomConfig` is the `name` defined in the `rewrite.yml` file.
> - For a single recipe, you don't even need to have the `rewrite.yml` file to customize the configuration, see [here](https://docs.openrewrite.org/reference/faq#is-it-possible-to-pass-arguments-to-a-recipe-from-the-command-line).

## For Developers

For details on how to contribute, set up the development environment, run tests, and other development-related information, please see our [CONTRIBUTING.md](CONTRIBUTING.md) file.
