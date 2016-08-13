package ch.retorte.intervalmusiccompositor.messagebus;

import com.google.common.annotations.VisibleForTesting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author nw
 */
public class DebugMessage implements StringMessage {

  private static final String DATE_FORMAT = "yyyy-MM-dd--HH:mm:ss.SSS";
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

  private final String callerClassName;
  private final String message;
  private final LocalDateTime date;

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

  String getCallerClassName() {
    return callerClassName;
  }

  public String getMessage() {
    return message;
  }

  LocalDateTime getDate() {
    return date;
  }

  private static String stacktraceFrom(Throwable t) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    t.printStackTrace(printWriter);
    return stringWriter.toString();
  }

  @VisibleForTesting
  String getFormattedDate() {
    return format(getDate());
  }

  public static String format(LocalDateTime localDateTime) {
    return dateFormatter.format(localDateTime);
  }
}
