package io.github.mhagnumdw.processors;

/**
 * This class processes the Query annotation from Jakarta Data Repository.
 */
public final class QueryAnnotationProcessor extends AnnotationOnlyOneArgumentProcessor {

    public static final AnnotationProcessor INSTANCE = new QueryAnnotationProcessor();

    private QueryAnnotationProcessor() {
    }

    @Override
    public String getFQN() {
        return "jakarta.data.repository.Query";
    }

}
