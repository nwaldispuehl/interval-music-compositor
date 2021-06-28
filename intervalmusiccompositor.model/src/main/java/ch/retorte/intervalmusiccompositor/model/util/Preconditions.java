package ch.retorte.intervalmusiccompositor.model.util;

/**
 * Fail fast with preconditions.
 */
public class Preconditions {

    public static <T> T checkNotNull(T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        return t;
    }
}
