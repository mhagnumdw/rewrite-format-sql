package io.github.mhagnumdw.processors;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

/**
 * This interface defines the contract for processing annotations that contain an annotation with SQL/HQL.
 * Implementations of this interface are responsible for extracting the query from the annotation,
 * formatting it, and updating the annotation with the formatted query.
 */
public interface AnnotationProcessor {

    /**
     * Returns the fully qualified name (FQN) of the annotation that this processor handles.
     */
    String getFQN();

    /**
     * Processes the given annotation by extracting the SQL/HQL query, formatting it,
     * and updating the annotation with the formatted query.
     *
     * @param annotation the annotation to process
     * @param cursor the cursor pointing to the context in which the annotation is found
     * @param dialect the SQL dialect to use for formatting
     * @param formatConfig the configuration for formatting
     * @return the updated annotation with the formatted query
     */
    J.Annotation process(J.Annotation annotation, Cursor cursor, Dialect dialect, FormatConfig formatConfig);

}
