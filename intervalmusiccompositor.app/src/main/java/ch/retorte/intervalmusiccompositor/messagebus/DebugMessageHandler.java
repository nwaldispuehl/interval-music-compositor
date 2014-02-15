package ch.retorte.intervalmusiccompositor.messagebus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;

/**
 * Message handler which collects debug messages and writes them to an appropriate place, currently the standard out.
 * 
 * @author nw.
 */
public class DebugMessageHandler implements MessageHandler<DebugMessage> {

  private static final String PART_DELIMITER = " - ";
  private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-DD--HH:mm:ss.SSS");

  @Override
  public void handle(DebugMessage message) {

    StringBuilder builder = new StringBuilder();

    builder.append(dateFormatter.format(message.getDate()));
    builder.append(PART_DELIMITER);
    builder.append(message.getCallerClassName());
    builder.append(PART_DELIMITER);
    builder.append(message.getMessage());

    System.out.println(builder.toString());
  }
}
