package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Some often used routines for threads.
 */
public class ThreadHelper {

  private final MessageProducer messageProducer;

  public ThreadHelper(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  public void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      addDebugMessage(e.getMessage());
    }
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
