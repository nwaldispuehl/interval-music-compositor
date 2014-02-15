package ch.retorte.intervalmusiccompositor.messagebus;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;

/**
 * Message handler that writes message content to the standard out (aka the console).
 * 
 * @author nw
 */
public class ConsoleMessageHandler implements MessageHandler<ConsoleMessage> {

  @Override
  public void handle(ConsoleMessage message) {
    if (message instanceof InfoMessage) {
      System.out.println(message.getMessage());
    }
    else if (message instanceof ErrorMessage) {
      System.err.println(message.getMessage());
    }
  }

}
