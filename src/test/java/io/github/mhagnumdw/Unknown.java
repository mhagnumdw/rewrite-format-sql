package io.github.mhagnumdw;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is for test purposes only, used in
 * tests where the Recipe should not modify the
 * annotation's text block content. It indicates that the
 * annotation is not known to the Recipe and therefore
 * should not be modified.
 */
@Target(METHOD)
@Retention(CLASS)
public @interface Unknown {
    String value();
}
