package ch.retorte.intervalmusiccompositor.ui.preferences;

import ch.retorte.intervalmusiccompositor.commons.preferences.UserPreferences;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

public class UiUserPreferences extends UserPreferences {

  //---- Static

  private static final String MUSIC_TRACK_LIST_KEY = "musicTrackList";
  private static final String BREAK_TRACK_LIST_KEY = "breakTrackList";
  private static final String ENUMERATION_MODE_KEY = "enumerationMode";

  private static final String SOUND_PERIOD_KEY = "soundPeriod";
  private static final String BREAK_PERIOD_KEY = "breakPeriod";
  private static final String SOUND_PATTERN_KEY = "soundPattern";
  private static final String BREAK_PATTERN_KEY = "breakPattern";
  private static final String PERIOD_TAB_KEY = "periodTab";

  private static final String ITERATIONS_KEY = "iterations";
  private static final String BLEND_MODE_KEY = "blendMode";
  private static final String BLEND_DURATION_KEY = "blendDuration";
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

  public void savePeriodTab(String periodTabId) {
    saveString(PERIOD_TAB_KEY, periodTabId);
  }

  public String loadPeriodTab(String defaultPeriodTabId) {
    return loadString(PERIOD_TAB_KEY, defaultPeriodTabId);
  }

  public void saveIterations(int iterations) {
    saveInt(ITERATIONS_KEY, iterations);
  }

  public int loadIterations(int defaultIterations) {
    return loadInt(ITERATIONS_KEY, defaultIterations);
  }

  public void saveBlendMode(String blendMode) {
    saveString(BLEND_MODE_KEY, blendMode);
  }

  public String loadBlendMode(String defaultBlendMode) {
   return loadString(BLEND_MODE_KEY, defaultBlendMode);
  }

  public void saveBlendDuration(int blendDuration) {
    saveInt(BLEND_DURATION_KEY, blendDuration);
  }

  public int loadBlendDuration(int defaultBlendDuration) {
    return loadInt(BLEND_DURATION_KEY, defaultBlendDuration);
  }

  public void saveOutputFileFormat(String outputFileFormat) {
    saveString(OUTPUT_FILE_FORMAT_KEY, outputFileFormat);
  }

  public String loadOutputFileFormat(String defaultOutputFileFormat) {
    return loadString(OUTPUT_FILE_FORMAT_KEY, defaultOutputFileFormat);
  }

  public void saveOutputDirectory(String outputDirectory) {
    saveString(OUTPUT_DIRECTORY_KEY, outputDirectory);
  }

  public boolean hasOutputDirectory() {
    return has(OUTPUT_DIRECTORY_KEY);
  }

  public String loadOutputDirectory(String defaultOutputDirectory) {
    return loadString(OUTPUT_DIRECTORY_KEY, defaultOutputDirectory);
  }

}
