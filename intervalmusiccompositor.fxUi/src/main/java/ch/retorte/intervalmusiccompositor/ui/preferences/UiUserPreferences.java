package ch.retorte.intervalmusiccompositor.ui.preferences;

import ch.retorte.intervalmusiccompositor.commons.preferences.UserPreferences;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

public class UiUserPreferences extends UserPreferences {

  //---- Static

  private static final String MUSIC_TRACK_LIST_KEY = "musicTrackList";
  private static final String BREAK_TRACK_LIST_KEY = "breakTrackList";

  private static final String SOUND_PERIOD_KEY = "soundPeriod";
  private static final String BREAK_PERIOD_KEY = "breakPeriod";
  private static final String SOUND_PATTERN_KEY = "soundPattern";
  private static final String BREAK_PATTERN_KEY = "breakPattern";

  private static final String ITERATIONS_KEY = "iterations";
  private static final String FADE_MODE_KEY = "fadeMode";
  private static final String FADE_DURATION_KEY = "fadeDuration";
  private static final String OUTPUT_FILE_FORMAT_KEY = "outputFileFormat";
  private static final String OUTPUT_DIRECTORY_KEY = "outputDirectory";


  public UiUserPreferences(MessageProducer messageProducer) {
    super(messageProducer);
  }

  //---- Methods

  public void saveSoundPeriod(int soundPeriod) {
    saveInt(SOUND_PERIOD_KEY, soundPeriod);
  }

  public int loadSoundPeriod(int defaultSoundPeriodValue) {
    return loadInt(SOUND_PERIOD_KEY, defaultSoundPeriodValue);
  }

  public void saveBreakPeriod(int breakPeriod) {
    saveInt(BREAK_PERIOD_KEY, breakPeriod);
  }

  public int loadBreakPeriod(int defaultBreakPeriodValue) {
    return loadInt(BREAK_PERIOD_KEY, defaultBreakPeriodValue);
  }

  public void saveSoundPattern(String soundPattern) {
    saveString(SOUND_PATTERN_KEY, soundPattern);
  }

  public String loadSoundPattern(String defaultSoundPatternValue) {
    return loadString(SOUND_PATTERN_KEY, defaultSoundPatternValue);
  }

  public void saveBreakPattern(String breakPattern) {
    saveString(BREAK_PATTERN_KEY, breakPattern);
  }

  public String loadBreakPattern(String defaultBreakPatternValue) {
    return loadString(BREAK_PATTERN_KEY, defaultBreakPatternValue);
  }



}
