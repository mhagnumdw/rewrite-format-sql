# rewrite-format-sql <!-- omit in toc -->

[![pipeline status](https://git.meugit.com.br/dwouglas/rewrite-format-sql/badges/main/pipeline.svg)](https://git.meugit.com.br/dwouglas/rewrite-format-sql/commits/main)
[![coverage report](https://git.meugit.com.br/dwouglas/rewrite-format-sql/badges/main/coverage.svg)](https://git.meugit.com.br/dwouglas/rewrite-format-sql)

Um conjunto de recipes do [OpenRewrite](https://docs.openrewrite.org/) para formatar código SQL/HQL.

- [Recipes](#recipes)
  - [FormatSqlBlockRecipe](#formatsqlblockrecipe)
  - [FormatSqlFileRecipe](#formatsqlfilerecipe)
- [Opções Configuráveis](#opções-configuráveis)
- [Exemplos](#exemplos)
  - [Exemplo FormatSqlBlockRecipe](#exemplo-formatsqlblockrecipe)
  - [Exemplo FormatSqlFileRecipe](#exemplo-formatsqlfilerecipe)
- [Fazendo Uso](#fazendo-uso)
  - [Configurando no `pom.xml`](#configurando-no-pomxml)
  - [Sem adicionar nada ao projeto](#sem-adicionar-nada-ao-projeto)
- [Para desenvolvedores](#para-desenvolvedores)

## Recipes

A seguir, uma descrição detalhada de cada Recipe.

### FormatSqlBlockRecipe

O recipe `io.github.mhagnumdw.FormatSqlBlockRecipe` formata automaticamente SQL ou HQL embutidos em [Text Blocks](https://docs.oracle.com/en/java/javase/13/text_blocks/index.html) presentes nas seguintes anotações:

- `org.hibernate.annotations.processing.HQL`
- `org.hibernate.annotations.processing.SQL`
- `jakarta.data.repository.Query`

> Melhorias futuras podem permitir a configuração de anotações personalizadas. Por favor, abra uma solicitação.

### FormatSqlFileRecipe

O recipe `io.github.mhagnumdw.FormatSqlFileRecipe` formata automaticamente o conteúdo de arquivos SQL.

## Opções Configuráveis

As seguintes opções são aplicáveis a ambos os Recipes `FormatSqlBlockRecipe` e `FormatSqlFileRecipe`:

| Tipo    | Nome              | Descrição                                                                                                                                                                                           | Exemplo  | Valor Padrão |
| :------ | :---------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  | :------- | :------------------------- |
| String  | `filePath`        | Opcional. O caminho para os arquivos que o Recipe deve processar. Aceita uma glob expression; múltiplos padrões podem ser especificados, separados por ponto e vírgula `;`. Se omitido, processa todos os arquivos correspondentes. | `**/*Repository.java` <br> `**/*.sql` | FormatSqlBlockRecipe: `**/*.java` <br> FormatSqlFileRecipe: `**/*.sql` |
| String  | `sqlDialect`      | Opcional. O dialeto SQL a ser usado para formatação. Opções válidas: `sql` (StandardSql), `mysql`, `postgresql`, `db2`, `plsql` (Oracle PL/SQL), `n1ql` (Couchbase N1QL), `redshift`, `spark`, `tsql` (SQL Server Transact-SQL). Detalhes [aqui](https://github.com/vertical-blank/sql-formatter). | `plsql`  | `sql` |
| String  | `indent`          | Opcional. A string a ser usada para indentação.                                                                                                                                                     | `"  "` para 2 espaços <br> `"\t"` para um tab | 4 espaços `"    "` |
| Integer | `maxColumnLength` | Opcional. O comprimento máximo de uma linha antes que o formatador tente quebrá-la.                                                                                                              | `100`    | `120` |
| Boolean | `uppercase`       | Opcional. Se deve converter palavras-chave SQL para maiúsculas (não é seguro usar quando o dialeto SQL tem identificadores sensíveis a maiúsculas/minúsculas).                                        | `true`   | `false` |

## Exemplos

### Exemplo FormatSqlBlockRecipe

Antes

```java
package com.mycompany;

import jakarta.data.repository.Query;

public interface HolidayRepository {
    @Query("""
        select h.*, c.name as country_name from Holiday h inner join Country c on h.country_id = c.id where h.year = :year and h.name != 'xpto' order by h.name""")
    void findByYear(int year);
}
```

Depois

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
            and h.name != 'xpto'
        order by
            h.name""")
    void findByYear(int year);
}
```

### Exemplo FormatSqlFileRecipe

Considere o seguinte arquivo `example.sql`:

```sql
select * from users where id = 1;
--
select d.name, c.name from City c inner join Department d on c.id = d.city_id;
```

Depois da execução do recipe, o arquivo `example.sql` será:

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

> Existe uma issue aberta sobre o espaço em alguns alias: <https://github.com/vertical-blank/sql-formatter/issues/77>

## Fazendo Uso

Abaixo são descritos os modos de uso. É possível aplicar um ou ambos os Recipes.

### Configurando no `pom.xml`

Se você tem um projeto e vai executar o recipe regularmente, essa é a forma recomenda.

Dentro da seção de plugins, adicionar:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.11.0</version>
    <configuration>
        <activeRecipes>
            <!-- Aqui os recipes que deseja usar -->
            <recipe>io.github.mhagnumdw.FormatSqlBlockRecipe</recipe>
            <recipe>io.github.mhagnumdw.FormatSqlFileRecipe</recipe>
        </activeRecipes>
        <failOnDryRunResults>false</failOnDryRunResults>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>io.github.mhagnumdw</groupId>
            <artifactId>rewrite-format-sql</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</plugin>
```

Então executar:

```bash
./mvnw rewrite:run
```

Para personalizar a configuração do recipe, é necessário ter o arquivo `rewrite.yml` na raiz do projeto. Exemplo:

```yml
---
type: specs.openrewrite.org/v1beta/recipe
name: io.github.mhagnumdw.FormatSqlCustomConfig
recipeList:
  # Aqui as Recipes que deseja usar
  - io.github.mhagnumdw.FormatSqlBlockRecipe:
      sqlDialect: "plsql"
  - io.github.mhagnumdw.FormatSqlFileRecipe:
      sqlDialect: "mysql"
```

> - Esse arquivo é uma forma de criar uma recipe sua customizada a partir de outras recipes.
> - O atributo `name` é arbitrário, mas é bom que ele remeta ao seu propósito. É o nome da recipe customizada.
> - O arquivo `rewrite.yml` deve ser versionado.
> - Para mais detalhes sobre o `rewrite.yml`, ver [aqui](https://docs.openrewrite.org/reference/yaml-format-reference).

E alterar a tag `<recipe>` no `pom.xml` para:

```xml
<recipe>io.github.mhagnumdw.FormatSqlCustomConfig</recipe>
```

> Como nesse exemplo a recipe `FormatSqlCustomConfig` engloba as duas recipes `FormatSqlBlockRecipe` e `FormatSqlFileRecipe`, no `pom.xml` só é necessário definir a recipe `FormatSqlCustomConfig`.

Então executar:

```bash
./mvnw rewrite:run
```

Para mais detalhes sobre a configuração do OpenRewrite com o maven, ver [aqui](https://docs.openrewrite.org/reference/rewrite-maven-plugin).

### Sem adicionar nada ao projeto

Esse modo é indicado se sua intenção é executar a recipe uma única vez.

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.FormatSqlBlockRecipe,io.github.mhagnumdw.FormatSqlFileRecipe \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0-SNAPSHOT
```

Para personalizar a configuração da recipe, é necessário ter o arquivo `rewrite.yml` na raiz do projeto, **conforme o exemplo anterior**.

Então executar:

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.FormatSqlCustomConfig \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0-SNAPSHOT
```

> - `io.github.mhagnumdw.FormatSqlCustomConfig` é o `name` definido no arquivo `rewrite.yml`.
> - Para uma única recipe nem é necessário ter o arquivo `rewrite.yml` para personalizar a configuração, ver [aqui](https://docs.openrewrite.org/reference/faq#is-it-possible-to-pass-arguments-to-a-recipe-from-the-command-line).

## Para desenvolvedores

Esse projeto utiliza Java 8 para o source principal e Java 17 para o source do teste. Ao importar no Eclipse como projeto maven, é preciso alterar manualmente para Java 17: clique com o `botão direito no projeto > Build Path > Configure Build Path... > Libraries`, remova o Java 8 e adicione o Java 17 usando o botão `Add Library...`.

Para testar no ambiente real em tempo de desenvolvimento, basta instalar o JAR e fazer referência para a versão SNAPSHOT:

```bash
./mvnw -V install
```

> O artefato é instalado no `~/.m2` com o GAV: `io.github.mhagnumdw:rewrite-format-sql:XXX-SNAPSHOT`

Para executar os testes automatizados:

```bash
./mvnw test
```

Cobertura de testes disponível em `target/site/jacoco/index.html`.

Para executar a formatação de código automática:

```bash
# formatar (CUIDADO, pois o código será modificado!)
./mvnw rewrite:run
# apenas validar
./mvnw rewrite:dryRun
```

O projeto foi criado inicialmente seguindo os guias oficiais:

- <https://docs.openrewrite.org/authoring-recipes/recipe-development-environment>
- <https://docs.openrewrite.org/authoring-recipes/writing-a-java-refactoring-recipe>
