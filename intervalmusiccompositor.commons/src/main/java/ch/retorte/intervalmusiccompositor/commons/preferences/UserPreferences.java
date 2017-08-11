package ch.retorte.intervalmusiccompositor.commons.preferences;

import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import java.util.prefs.Preferences;

/**
 * Allows access to persistent user preferences.
 */
public class UserPreferences {




  //---- Fields

  private MessageProducer messageProducer;

  private final Preferences preferences;


  //---- Constructor

  public UserPreferences(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
    preferences = Preferences.userNodeForPackage(UserPreferences.class);
  }


  //---- Methods






  //---- Helper methods

  protected void saveInt(String key, int value) {
    addSaveDebugMessageFor(key, value);
    preferences.putInt(key, value);
  }

  protected int loadInt(String key, int defaultValue) {
    int result = preferences.getInt(key, defaultValue);
    addLoadDebugMessageFor(key, result);
    return result;
  }

  protected void saveString(String key, String value) {
    addSaveDebugMessageFor(key, value);
    preferences.put(key, value);
  }

  protected String loadString(String key, String defaultValue) {
    String result = preferences.get(key, defaultValue);
    addLoadDebugMessageFor(key, result);
    return result;
  }

  private void addSaveDebugMessageFor(String key, Object value) {
    addDebugMessage("Storing user preference '" + key + "' value: " + value);
  }

  private void addLoadDebugMessageFor(String key, Object value) {
    addDebugMessage("Loading user preference '" + key + "' value: " + value);
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void addDebugMessage(Throwable throwable) {
    messageProducer.send(new DebugMessage(this, throwable));
  }

}
