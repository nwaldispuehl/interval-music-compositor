package ch.retorte.intervalmusiccompositor.messagebus;

/**
 * Message intended to be published on standard out (aka the console).
 * 
 * @author nw
 */
class ConsoleMessage implements StringMessage {

  private final String message;

  ConsoleMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
