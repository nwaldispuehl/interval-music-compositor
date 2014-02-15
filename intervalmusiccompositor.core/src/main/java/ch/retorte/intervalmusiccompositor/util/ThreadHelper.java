package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * @author nw
 */
public class ThreadHelper {

  private MessageProducer messageProducer;

  public ThreadHelper(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  public void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      messageProducer.send(new DebugMessage(this, e.getMessage()));
    }
  }
}
