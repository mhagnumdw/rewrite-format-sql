# rewrite-format-sql

Recipe do OpenRewrite para formatar blocos de código SQL/HQL dentro de código Java.

⚠️ Esse projeto está em fase muito embrionária. **E no momento contém apenas código de estudo.** O nome do Recipe no momento é `SayHelloRecipe` e no futuro talvez seja `FormatSqlBlockRecipe`. ⚠️

Esse projeto utiliza Java 8 para o source principal e Java 17 para o source do teste. Ao importar no Eclipse como projeto maven, é preciso alterar manualmente para Java 17: clique com o `botão direito no projeto > Build Path > Configure Build Path... > Libraries`, remova o Java 8 e adicione o Java 17 usando o botão `Add Library...`.

Esse projeto foi criado inicialmente seguindo os guias oficiais:

- <https://docs.openrewrite.org/authoring-recipes/recipe-development-environment>
- <https://docs.openrewrite.org/authoring-recipes/writing-a-java-refactoring-recipe>

## O que o SayHelloRecipe faz

Nome completo do recipe: `io.github.mhagnumdw.SayHelloRecipe`

O `SayHelloRecipe` adicionará um método `hello()` a uma classe especificada pelo usuário, caso essa classe ainda não tenha um. O método adicionado será esse:

```java
public String hello() {
    return "Hello from <fqn-da-classe-especificada-pelo-usuário-aqui>!";
}
```

## Fazendo uso do SayHelloRecipe

Empacotar e instalar:

```bash
mvn -V clean install
```

> Nesse momento o artefato é instalado no `~/.m2` come esse GAV: `io.github.mhagnumdw:rewrite-format-sql:1.0-SNAPSHOT`

Agora vamos supor que queremos aplicar esse Recipe a classe `io.github.mhagnumdw.FooBar` existente em um projeto qualquer. Essa:

```java
package io.github.mhagnumdw;
class FooBar {

}
```

Na raiz do projeto, criar o arquivo `rewrite.yml`:

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: io.github.mhagnumdw.SayHelloRecipeMyConf # um nome único qualquer
recipeList:
  - io.github.mhagnumdw.SayHelloRecipe: # o nome completo do Recipe
      fullyQualifiedClassName: io.github.mhagnumdw.FooBar # que classe o Recipe deve alterar
```

> - Mais detalhes sobre esse yaml: <https://docs.openrewrite.org/reference/yaml-format-reference>
> - Se essa for uma configuração que será executada com frequência, esse arquivo pode ser versionado
> - Essa configuração também pode ser feito no `pom.xml`, como é mencionada mais abaixo

Executar o Recipe:

```bash
mvn -V org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.mhagnumdw.SayHelloRecipeMyConf \
  -Drewrite.recipeArtifactCoordinates=io.github.mhagnumdw:rewrite-format-sql:1.0-SNAPSHOT
```

> Parâmetros:
>
> - `rewrite.activeRecipes`: é o `name` no arquivo `rewrite.yml` mais acima
> - `rewrite.recipeArtifactCoordinates`: é o GAV do JAR do Recipe

Agora a classe `FooBar` terá o método `hello()` adicionado:

```java
package io.github.mhagnumdw;

class FooBar {
    public String hello() {
        return "Hello from com.yourorg.FooBar!";
    }
}
```

> O Recipe `io.github.mhagnumdw.SayHelloRecipe` também pode ser definido e configurado no `pom.xml` do projeto. Desse modo, ao executar apenas `mvn -V org.openrewrite.maven:rewrite-maven-plugin:run` ou mais simplesmente `mvn rewrite:run`, todos os Recipes configurados no `pom.xml` serão executados.
