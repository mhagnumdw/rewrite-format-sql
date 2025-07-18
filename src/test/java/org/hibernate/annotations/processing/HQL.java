//
// Copied, reason: so OpenRewrite can find it on the classpath during tests
//
package org.hibernate.annotations.processing;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(METHOD)
@Retention(CLASS)
public @interface HQL {
	String value();
}
