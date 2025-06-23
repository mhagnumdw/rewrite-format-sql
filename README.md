# rewrite-format-sql <!-- omit in toc -->

[![pipeline status](https://git.sefin.fortaleza.ce.gov.br/dwouglas/rewrite-format-sql/badges/main/pipeline.svg)](https://git.sefin.fortaleza.ce.gov.br/dwouglas/rewrite-format-sql/commits/main)
[![coverage report](https://git.sefin.fortaleza.ce.gov.br/dwouglas/rewrite-format-sql/badges/main/coverage.svg)](https://git.sefin.fortaleza.ce.gov.br/dwouglas/rewrite-format-sql)

Um Recipe do OpenRewrite para formatar blocos de código SQL/HQL dentro de código Java.

O recipe `io.github.mhagnumdw.FormatSqlBlockRecipe` formata automaticamente SQL ou HQL embutidos em [Text Blocks](https://docs.oracle.com/en/java/javase/13/text_blocks/index.html) presente em algumas anotações.

- [Anotações que são suportadas](#anotações-que-são-suportadas)
- [Opções configuráveis](#opções-configuráveis)
- [Exemplo](#exemplo)
- [Fazendo uso](#fazendo-uso)
  - [Configurando no `pom.xml`](#configurando-no-pomxml)
  - [Sem adicionar nada ao projeto](#sem-adicionar-nada-ao-projeto)
- [Para desenvolvedores](#para-desenvolvedores)

## Anotações que são suportadas

O `FormatSqlBlockRecipe` formata SQL/HQL presentes em Text Blocks nas seguintes anotações:

- `org.hibernate.annotations.processing.HQL`
- `org.hibernate.annotations.processing.SQL`
- `jakarta.data.repository.Query`

Melhorias futuras podem permitir a configuração de anotações personalizadas.

Engine de formatação: [com.github.vertical-blank:sql-formatter](https://github.com/vertical-blank/sql-formatter)

## Opções configuráveis

| Tipo    | Nome              | Descrição                                                                                                                                                                                           | Exemplo  |
| :------ | :---------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  | :------- |
| String  | `filePath`   | Opcional. O caminho para os arquivos Java que o Recipe deve processar. Aceita uma glob expression, with a multiple patterns may be specified, separated by a semicolon `;`. Defaults to `**/*.java`.| `**/*Repository.java` |
| String  | `sqlDialect`      | Opcional. The dialect to use for formatting. Opções válidas: `sql` (StandardSql), `mysql`, `postgresql`, `db2`, `plsql` (Oracle PL/SQL), `n1ql` (Couchbase N1QL), `redshift`, `spark`, `tsql` (SQL Server Transact-SQL). Detalhes [aqui](https://github.com/vertical-blank/sql-formatter). Defaults to `sql`. | `plsql` |
| String  | `indent`          | Opcional. A string a ser usada para indentação. Defaults to 4 spaces. | `"    "` para quatro espaços, `"\t"` para um tab |
| Integer | `maxColumnLength` | Opcional. O comprimento máximo de uma linha antes que o formatador tente quebrá-la.Defaults to `120`. | `100` |
| Boolean | `uppercase`       | Opcional. Se deve converter palavras-chave SQL para maiúsculas. O padrão é `false`. | `false`                                                                                                                               |

## Exemplo

Antes

```java
package com.mycompany;

import jakarta.data.repository.Query;

public interface HolidayRepository {
    @Query("""
        select * from Holiday where year = :year order by name""")
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
            *
        from
            Holiday
        where
            year = :year
        order by
            name""")
    void findByYear(int year);
}
```

## Fazendo uso

Abaixo três modos de uso.

### Configurando no `pom.xml`

Se você tem um projeto e vai executar essa Recipe regularmente, essa é a forma recomenda.

Dentro da seção de plugins, adicionar:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.11.0</version>
    <configuration>
        <activeRecipes>
            <recipe>io.github.mhagnumdw.FormatSqlBlockRecipe</recipe>
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

Para personalizar a configuração do Recipe, é necessário ter o arquivo `rewrite.yml` na raiz do projeto. Exemplo:

```yml
---
type: specs.openrewrite.org/v1beta/recipe
name: io.github.mhagnumdw.FormatSqlBlockRecipe_Configured
recipeList:
  - io.github.mhagnumdw.FormatSqlBlockRecipe:
      sqlDialect: "plsql"
```

> - O atributo `name` é arbitrário. Observar que **não** é o nome do Recipe. O Recipe em si foi definido no atributo `recipeList`.
> - O arquivo `rewrite.yml` deve ser versionado.
> - Mais detalhes sobre o `rewrite.yml`, ver [aqui](https://docs.openrewrite.org/reference/yaml-format-reference).

E alterar a tag `<recipe>` no `pom.xml` para:

```xml
<recipe>io.github.mhagnumdw.FormatSqlBlockRecipe_Configured</recipe>
```

Então executar:

```bash
./mvnw rewrite:run
```

Para mais detalhes sobre a configuração do OpenRewrite com o maven, ver [aqui](https://docs.openrewrite.org/reference/rewrite-maven-plugin).

### Sem adicionar nada ao projeto

Esse modo é indicado se sua intenção é executar esse Recipe uma única vez.

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.FormatSqlBlockRecipe \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0-SNAPSHOT
```

Para personalizar a configuração do Recipe, é necessário ter o arquivo `rewrite.yml` na raiz do projeto, **conforme o exemplo anterior**.

Então executar:

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.FormatSqlBlockRecipe_Configured \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0-SNAPSHOT
```

> `io.github.mhagnumdw.FormatSqlBlockRecipe_Configured` é o `name` definido no arquivo `rewrite.yml`.
> Para uma única Recipe nem é necessário ter o arquivo `rewrite.yml` para personalizar a configuração, ver [aqui](https://docs.openrewrite.org/reference/faq#is-it-possible-to-pass-arguments-to-a-recipe-from-the-command-line).

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
