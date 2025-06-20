package io.github.mhagnumdw.processors;

/**
 * This class processes the HQL annotation from Hibernate.
 */
public final class HQLAnnotationProcessor extends AnnotationOnlyOneArgumentProcessor {

    public static final AnnotationProcessor INSTANCE = new HQLAnnotationProcessor();

    private HQLAnnotationProcessor() {
    }

    @Override
    public String getFQN() {
        return "org.hibernate.annotations.processing.HQL";
    }

}
