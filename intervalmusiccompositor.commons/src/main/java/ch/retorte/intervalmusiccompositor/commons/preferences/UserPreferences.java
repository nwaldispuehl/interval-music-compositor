package ch.retorte.intervalmusiccompositor.commons.preferences;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Allows access to persistent user preferences.
 */
public class UserPreferences {

  //---- Static

  private static final String LAST_PROGRAM_VERSION_KEY = "lastProgramVersion";
  private static final String LAST_START_KEY = "lastStart";
  private static final String LOCALE_KEY = "locale";
  private static final String SAVE_FIELD_STATE_KEY = "keepFieldState";
  private static final String SEARCH_UPDATE_AT_STARTUP_KEY = "searchUpdateAtStartup";


  //---- Fields

  private MessageProducer messageProducer;

  private final Preferences preferences;


  //---- Constructor

  public UserPreferences(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
    preferences = Preferences.userNodeForPackage(UserPreferences.class);
  }


  //---- Methods

  public void saveLastProgramVersion(Version version) {
    saveString(LAST_PROGRAM_VERSION_KEY, version.toString());
  }

  public Version loadLastProgramVersion() {
    String loadedVersionString = loadString(LAST_PROGRAM_VERSION_KEY, "");
    return new Version(loadedVersionString);
  }

  public void saveLastStart() {
    saveString(LAST_START_KEY, LocalDateTime.now().toString());
  }

  public void saveLocale(Locale locale) {
    saveString(LOCALE_KEY, locale.getLanguage());
  }

  public boolean hasLocale() {
    return has(LOCALE_KEY);
  }

  public Locale loadLocale() {
    String loadedLocaleString = loadString(LOCALE_KEY, null);
    return Locale.forLanguageTag(loadedLocaleString);
  }

  public void saveSaveFieldState(boolean saveFieldState) {
    saveBoolean(SAVE_FIELD_STATE_KEY, saveFieldState);
  }

  public boolean loadSaveFieldState() {
    return loadBoolean(SAVE_FIELD_STATE_KEY, true);
  }

  public void saveSearchUpdateAtStartup(boolean searchUpdateAtStartup) {
    saveBoolean(SEARCH_UPDATE_AT_STARTUP_KEY, searchUpdateAtStartup);
  }

  public boolean loadSearchUpdateAtStartup() {
    return loadBoolean(SEARCH_UPDATE_AT_STARTUP_KEY, true);
  }


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

    if (value != null) {
      addSaveDebugMessageFor(key, value);
      preferences.put(key, value);
      flush();
    }
    else {
      addClearDebugMessageFor(key);
      preferences.remove(key);
      flush();
    }
  }

  protected String loadString(String key, String defaultValue) {
    String result = preferences.get(key, defaultValue);
    addLoadDebugMessageFor(key, result);
    return result;
  }

  protected void saveBoolean(String key, boolean value) {
    addSaveDebugMessageFor(key, value);
    preferences.putBoolean(key, value);
    flush();
  }

  protected boolean loadBoolean(String key, boolean defaultValue) {
    boolean result = preferences.getBoolean(key, defaultValue);
    addLoadDebugMessageFor(key, result);
    return result;
  }

  protected boolean has(String key) {
    return preferences.get(key, null) != null;
  }

  /**
   * Causes the changes to be written to the persistent backend instantly.
  */
  private void flush() {
    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      addDebugMessage("Not able to store preferences: " + e.getMessage(), e);
    }
  }

  private void addSaveDebugMessageFor(String key, Object value) {
    addDebugMessage("Storing user preference '" + key + "' value: " + value);
  }

  private void addClearDebugMessageFor(String key) {
    addDebugMessage("Removing user preference '" + key + "'");
  }

  private void addLoadDebugMessageFor(String key, Object value) {
    addDebugMessage("Loading user preference '" + key + "' value: " + value);
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void addDebugMessage(String message, Throwable throwable) {
    messageProducer.send(new DebugMessage(this, message, throwable));
  }

}
