package io.github.mhagnumdw.processors;

/**
 * This class processes the SQL annotation from Hibernate.
 */
public final class SQLAnnotationProcessor extends AnnotationOnlyOneArgumentProcessor {

    public static final AnnotationProcessor INSTANCE = new SQLAnnotationProcessor();

    private SQLAnnotationProcessor() {
    }

    @Override
    public String getFQN() {
        return "org.hibernate.annotations.processing.SQL";
    }

}
