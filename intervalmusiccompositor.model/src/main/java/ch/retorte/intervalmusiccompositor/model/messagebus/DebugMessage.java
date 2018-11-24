package ch.retorte.intervalmusiccompositor.model.messagebus;

import com.google.common.annotations.VisibleForTesting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Instance of the string message which is designed to be put on a debug log; i.e. formats date in a nice way.
 */
public class DebugMessage implements StringMessage {

  //---- Static

  private static final String DATE_FORMAT = "yyyy-MM-dd--HH:mm:ss.SSS";
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);


  //---- Fields

  private final String callerClassName;
  private final String message;
  private final LocalDateTime date;


  //---- Constructors

  public DebugMessage(String callerClassName, String message, LocalDateTime date) {
    this.callerClassName = callerClassName;
    this.message = message;
    this.date = date;
  }

  public DebugMessage(Object caller, String message) {
    this(caller.getClass().getSimpleName(), message, LocalDateTime.now());
  }

  public DebugMessage(Object caller, Throwable t) {
    this(caller, stacktraceFrom(t));
  }

  public DebugMessage(Object caller, String message, Throwable t) {
    this(caller, message + "\n" + stacktraceFrom(t));
  }


  //---- Methods

  public String getCallerClassName() {
    return callerClassName;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getDate() {
    return date;
  }

  private static String stacktraceFrom(Throwable t) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    t.printStackTrace(printWriter);
    return stringWriter.toString();
  }

  public String getFormattedDate() {
    return format(getDate());
  }

  public static String format(LocalDateTime localDateTime) {
    return dateFormatter.format(localDateTime);
  }
}
