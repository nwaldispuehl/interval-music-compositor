package ch.retorte.intervalmusiccompositor.compilation;

/**
 * Indicates that a compilation was not properly created.
 */
public class CompilationException extends IllegalStateException {

  CompilationException(String message, Throwable cause) {
    super(message, cause);
  }

}
