package ch.retorte.intervalmusiccompositor.model.util;

/**
 * Functional interface for change listeners.
 */
@FunctionalInterface
public interface ChangeListener<T> {
  void changed(T newValue);
}
