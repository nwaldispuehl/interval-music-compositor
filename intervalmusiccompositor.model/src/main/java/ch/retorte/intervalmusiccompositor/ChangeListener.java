package ch.retorte.intervalmusiccompositor;

/**
 * Functional interface for change listeners.
 */
@FunctionalInterface
public interface ChangeListener<T> {
  void changed(T newValue);
}
