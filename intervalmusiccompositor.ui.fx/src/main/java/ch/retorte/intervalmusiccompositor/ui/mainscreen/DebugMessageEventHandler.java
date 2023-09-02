package ch.retorte.intervalmusiccompositor.ui.mainscreen;

import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.HumanReadableName;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

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
    if (!(newValue instanceof Node)) {
      messageProducer.send(new DebugMessage(caller, "Changed contents of field '" + id + "' from: '" + valueOf(oldValue) + "' to '" + valueOf(newValue) + "'"));
    }
  }

  String valueOf(Object o) {
    if (o == null) {
      return "<empty>";
    }
    else if (o instanceof HumanReadableName n) {
      return n.getName();
    }
    else {
      return o.toString();
    }
  }

}
