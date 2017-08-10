package ch.retorte.intervalmusiccompositor.commons.preferences;

import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import java.util.prefs.Preferences;

/**
 * Allows access to persistent user preferences.
 */
public class UserPreferences {

  //---- Static

  private static final String IMC_PREFIX = "imcUserPreferences-";

  private static final String MUSIC_TRACK_LIST_KEY = IMC_PREFIX + "musicTrackList";
  private static final String BREAK_TRACK_LIST_KEY = IMC_PREFIX + "breakTrackList";

  private static final String SOUND_PERIOD_KEY = IMC_PREFIX + "soundPeriod";
  private static final String BREAK_PERIOD_KEY = IMC_PREFIX + "breakPeriod";
  private static final String SOUND_PATTERN_KEY = IMC_PREFIX + "soundPattern";
  private static final String BREAK_PATTERN_KEY = IMC_PREFIX + "breakPattern";

  private static final String ITERATIONS_KEY = IMC_PREFIX + "iterations";
  private static final String FADE_MODE_KEY = IMC_PREFIX + "fadeMode";
  private static final String FADE_DURATION_KEY = IMC_PREFIX + "fadeDuration";
  private static final String OUTPUT_FILE_FORMAT_KEY = IMC_PREFIX + "outputFileFormat";
  private static final String OUTPUT_DIRECTORY_KEY = IMC_PREFIX + "outputDirectory";


  //---- Fields

  private MessageProducer messageProducer;

  private final Preferences preferences;


  //---- Constructor

  public UserPreferences(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
    preferences = Preferences.userNodeForPackage(UserPreferences.class);
  }


  //---- Methods

  public void saveSoundPeriod(int soundPeriod) {
    saveInt(SOUND_PERIOD_KEY, soundPeriod);
  }

  public int loadSoundPeriodWithDefault(int defaultValue) {
    return loadInt(SOUND_PERIOD_KEY, defaultValue);
  }


  //---- Helper methods

  private void saveInt(String key, int value) {
    addDebugMessage("Storing user preference '" + key + "' value: " + value);
    preferences.putInt(key, value);
  }

  private int loadInt(String key, int defaultValue) {
    int result = preferences.getInt(SOUND_PERIOD_KEY, defaultValue);
    addDebugMessage("Loading user preference '" + key + "' value: " + result);
    return result;
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void addDebugMessage(Throwable throwable) {
    messageProducer.send(new DebugMessage(this, throwable));
  }

}
