package ch.retorte.intervalmusiccompositor.messagebus;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;

/**
 * Message handler which collects debug messages and writes them to an appropriate place, currently the standard out.
 * 
 * @author nw.
 */
public class DebugMessagePrinter extends ConsolePrinter implements MessageHandler<DebugMessage> {

  private static final String PART_DELIMITER = " - ";

  @Override
  public void handle(DebugMessage message) {

    String builder = message.getFormattedDate() +
        PART_DELIMITER +
        message.getCallerClassName() +
        PART_DELIMITER +
        message.getMessage();

    printToStdOut(builder);
  }


}
