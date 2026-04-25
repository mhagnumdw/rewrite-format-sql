package io.github.mhagnumdw.processors;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * This enum defines the different annotation processors available for handling SQL/HQL annotations.
 */
public enum Annotations {

    HQL_HIBERNATE(HQLAnnotationProcessor.INSTANCE),
    QUERY_JAKARTA(QueryAnnotationProcessor.INSTANCE),
    SQL_HIBERNATE(SQLAnnotationProcessor.INSTANCE);

    @Getter
    private AnnotationProcessor processor;

    Annotations(AnnotationProcessor processor) {
        this.processor = processor;
    }

    /**
     * Returns the annotation processor for the given fully qualified name (FQN) of the annotation.
     * @param fqn the fully qualified name (FQN) of the annotation
     * @return an Optional containing the AnnotationProcessor if found, otherwise an empty Optional
     */
    public static Optional<AnnotationProcessor> getProcessor(String fqn) {
        return Arrays.stream(values())
            .map(Annotations::getProcessor)
            .filter(processor -> processor.getFQN().equals(fqn))
            .findFirst();
    }

}
