package io.github.mhagnumdw;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import io.github.mhagnumdw.processors.Annotations;

public class FormatSqlBlockVisitor extends JavaIsoVisitor<ExecutionContext> {

    private final Dialect dialect;
    private final FormatConfig formatConfig;

    FormatSqlBlockVisitor(Dialect dialect, FormatConfig formatConfig) {
        this.dialect = dialect;
        this.formatConfig = formatConfig;
    }

    @Override
    public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext context) {
        JavaType type = annotation.getType();

        if (type == null) {
            return annotation;
        }

        String annotationFQN = type.toString();

        return Annotations.getProcessor(annotationFQN)
            .map(p -> p.process(annotation, getCursor(), dialect, formatConfig))
            .orElse(annotation);
    }

}
