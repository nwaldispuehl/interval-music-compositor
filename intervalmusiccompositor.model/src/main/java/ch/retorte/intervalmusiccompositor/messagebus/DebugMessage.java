package ch.retorte.intervalmusiccompositor.messagebus;

import java.util.Date;

/**
 * @author nw
 */
public class DebugMessage implements Message {

  private final String callerClassName;
  private final String message;
  private final Date date;

  public DebugMessage(Object caller, String message) {
    this.callerClassName = caller.getClass().getSimpleName();
    this.message = message;
    this.date = new Date();
  }

  public String getCallerClassName() {
    return callerClassName;
  }

  public String getMessage() {
    return message;
  }

  public Date getDate() {
    return date;
  }

}
