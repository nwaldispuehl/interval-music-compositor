package ch.retorte.intervalmusiccompositor.compilation;

/**
 * Indicates that a compilation was not properly created.
 */
public class CompilationException extends IllegalStateException {

  private static final long serialVersionUID = 1L;

  CompilationException(String message, Throwable cause) {
    super(message, cause);
  }

}
