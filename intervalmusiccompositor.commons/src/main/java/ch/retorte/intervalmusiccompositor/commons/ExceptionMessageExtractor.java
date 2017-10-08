package ch.retorte.intervalmusiccompositor.commons;

/**
 * Some tool to get more meaningful exception messages.
 */
public class ExceptionMessageExtractor {

  public static String messageOrNameOf(Throwable throwable) {
    return throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
  }

}
