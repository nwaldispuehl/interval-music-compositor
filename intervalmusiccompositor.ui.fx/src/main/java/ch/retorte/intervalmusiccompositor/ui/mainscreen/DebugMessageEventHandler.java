package ch.retorte.intervalmusiccompositor.ui.mainscreen;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DebugMessageEventHandler implements ChangeListener<Object> {

  private final Object caller;
  private final String id;
  private final MessageProducer messageProducer;

  public DebugMessageEventHandler(Object caller, String id, MessageProducer messageProducer) {
    this.caller = caller;
    this.id = id;
    this.messageProducer = messageProducer;
  }

  @Override
  public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
    messageProducer.send(new DebugMessage(caller, "Changed contents of field '" + id + "' from: '" + oldValue + "' to '" + newValue + "'"));
  }
}
