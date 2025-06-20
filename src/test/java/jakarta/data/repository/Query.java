//
// Copied, reason: so OpenRewrite can find it on the classpath during tests
//
package jakarta.data.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {

    String value(); // JDQL or JPQL
}

