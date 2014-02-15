package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Message intended to be published on standard out (aka the console).
 * 
 * @author nw
 */
public class ConsoleMessage implements Message {

  private final String message;

  public ConsoleMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
