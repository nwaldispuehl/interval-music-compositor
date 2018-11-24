package ch.retorte.intervalmusiccompositor.model.messagebus;

/**
 * @author nw
 */
public class ErrorMessage extends ConsoleMessage {

  public ErrorMessage(String message) {
    super(message);
  }

  public ErrorMessage(Throwable throwable) {
    super(getMessageFrom(throwable));
  }

  private static String getMessageFrom(Throwable throwable) {
    String message = throwable.getMessage();
    if (message != null) {
      return message;
    }
    else {
      return throwable.getClass().getSimpleName();
    }
  }

}
