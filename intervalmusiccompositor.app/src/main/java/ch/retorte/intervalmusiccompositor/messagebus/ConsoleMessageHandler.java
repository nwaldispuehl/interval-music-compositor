package ch.retorte.intervalmusiccompositor.messagebus;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;

/**
 * Message handler that writes message content to the standard out (aka the console).
 * 
 * @author nw
 */
public class ConsoleMessageHandler extends ConsolePrinter implements MessageHandler<ConsoleMessage> {

  @Override
  public void handle(ConsoleMessage message) {
    if (message instanceof InfoMessage) {
      printToStdOut(message.getMessage());
    }
    else if (message instanceof ErrorMessage) {
      printToStdErr(message.getMessage());
    }
  }

}
