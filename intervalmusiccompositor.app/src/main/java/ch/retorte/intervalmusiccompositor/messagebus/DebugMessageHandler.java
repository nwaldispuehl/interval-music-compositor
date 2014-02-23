package ch.retorte.intervalmusiccompositor.messagebus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.common.annotations.VisibleForTesting;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;

/**
 * Message handler which collects debug messages and writes them to an appropriate place, currently the standard out.
 * 
 * @author nw.
 */
public class DebugMessageHandler extends ConsolePrinter implements MessageHandler<DebugMessage> {

  public static final String DATE_FORMAT = "yyyy-MM-DD--HH:mm:ss.SSS";

  private static final String PART_DELIMITER = " - ";
  private final DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

  @Override
  public void handle(DebugMessage message) {

    StringBuilder builder = new StringBuilder();

    builder.append(getFormattedDate(message));
    builder.append(PART_DELIMITER);
    builder.append(message.getCallerClassName());
    builder.append(PART_DELIMITER);
    builder.append(message.getMessage());

    printToStdOut(builder.toString());
  }

  @VisibleForTesting
  private String getFormattedDate(DebugMessage message) {
    return dateFormatter.format(message.getDate());
  }
}
