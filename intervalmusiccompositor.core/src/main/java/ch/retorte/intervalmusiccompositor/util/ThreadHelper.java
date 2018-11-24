package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
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
      addDebugMessage(e.getMessage());
    }
  }

  public void wait(Runnable t) {
    try {
      synchronized (t) {
        t.wait();
      }
    }
    catch (InterruptedException e) {
      addDebugMessage(e.getMessage());
    }
  }

  public void notify(Runnable t) {
    try {
      synchronized (t) {
        t.notify();
      }
    }
    catch (IllegalMonitorStateException e) {
      addDebugMessage(e.getMessage());
    }
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
