package ch.retorte.intervalmusiccompositor.ui.utils;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;

/**
 * Collection of tool methods for JavaFx widgets / controls.
 */
public class WidgetTools {

  /**
   * Links a spinners value property to its factory's value property which has the effect, that it also is notified on manual updates
   * (as opposed to pressing the spinner buttons).
   */
  public <T> void makeListeningForManualValueUpdates(Spinner<T> spinner) {
    SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
    TextFormatter<T> formatter = new TextFormatter<>(valueFactory.getConverter(), valueFactory.getValue());
    spinner.getEditor().setTextFormatter(formatter);
    valueFactory.valueProperty().bindBidirectional(formatter.valueProperty());
  }

  public void addNullTolerantConverterTo(Spinner<Integer> spinner) {
    spinner.getValueFactory().setConverter(new NullTolerantIntegerStringConverter());
  }
}
