package ch.retorte.intervalmusiccompositor.commons;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author nw
 */
public class MessageFormatBundle {
  private ResourceBundle bundle;

  public MessageFormatBundle(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  public String getString(String key, Object... arguments) {
    String value = bundle.getString(key);
    return MessageFormat.format(value, arguments);
  }

  public Locale getLocale() {
    return bundle.getLocale();
  }
}
