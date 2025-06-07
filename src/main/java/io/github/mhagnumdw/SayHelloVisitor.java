package io.github.mhagnumdw;

import org.jspecify.annotations.NonNull;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

// https://docs.openrewrite.org/authoring-recipes/writing-a-java-refactoring-recipe#filtering-out-classes-that-dont-match-the-fully-qualified-name
public class SayHelloVisitor extends JavaIsoVisitor<ExecutionContext> {

    private final JavaTemplate helloTemplate =
        JavaTemplate.builder( "public String hello() { return \"Hello from #{}!\"; }")
                .build();

    private String fullyQualifiedClassName;

    public SayHelloVisitor(@NonNull String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
        // Don't make changes to classes that don't match the fully qualified name
        if (classDecl.getType() == null || !classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
            return classDecl;
        }

        // Check if the class already has a method named "hello"
        boolean helloMethodExists = classDecl.getBody().getStatements().stream()
                .filter(statement -> statement instanceof J.MethodDeclaration)
                .map(J.MethodDeclaration.class::cast)
                .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("hello"));

        // If the class already has a `hello()` method, don't make any changes to it.
        if (helloMethodExists) {
            return classDecl;
        }

        // Interpolate the fullyQualifiedClassName into the template and use the resulting LST to update the class body
        classDecl = classDecl.withBody(
            helloTemplate.apply(
                new Cursor(getCursor(), classDecl.getBody()),
                classDecl.getBody().getCoordinates().lastStatement(),
                fullyQualifiedClassName
            )
        );

        return classDecl;
    }
}