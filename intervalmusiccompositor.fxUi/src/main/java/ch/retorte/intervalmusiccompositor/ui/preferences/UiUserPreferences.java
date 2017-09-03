package ch.retorte.intervalmusiccompositor.ui.preferences;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.preferences.UserPreferences;
import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

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

  private static final String SOUND_EFFECTS_KEY = "soundEffects";

  private static final String OUTPUT_FILE_FORMAT_KEY = "outputFileFormat";
  private static final String OUTPUT_DIRECTORY_KEY = "outputDirectory";


  //---- Fields

  private SimpleObjectProperty<Locale> localeProperty = new SimpleObjectProperty<>();
  private SimpleBooleanProperty saveFieldStateProperty = new SimpleBooleanProperty();
  private SimpleBooleanProperty searchUpdateAtStartupProperty = new SimpleBooleanProperty();


  //---- Constructor

  public UiUserPreferences(MessageProducer messageProducer) {
    super(messageProducer);

    localeProperty.setValue(loadLocale());
    localeProperty.addListener((observable, oldValue, newValue) -> saveLocale(newValue));

    saveFieldStateProperty.setValue(loadSaveFieldState());
    saveFieldStateProperty.addListener((observable, oldValue, newValue) -> saveSaveFieldState(newValue));

    searchUpdateAtStartupProperty.setValue(loadSearchUpdateAtStartup());
    searchUpdateAtStartupProperty.addListener((observable, oldValue, newValue) -> saveSearchUpdateAtStartup(newValue));
  }


  //---- Methods

  public Property<Locale> localeProperty() {
    return localeProperty;
  }

  public Property<Boolean> saveFieldStateProperty() {
    return saveFieldStateProperty;
  }

  public Property<Boolean> searchUpdateAtStartupProperty() {
    return searchUpdateAtStartupProperty;
  }

  public void saveMusicTrackList(List<? extends IAudioFile> musicTrackList) {
    saveString(MUSIC_TRACK_LIST_KEY, serializeFiles(musicTrackList.stream().map(IAudioFile::getSource).collect(toList())));
  }

  public List<File> loadMusicTrackList() {
    return deserializeFiles(loadString(MUSIC_TRACK_LIST_KEY, ""));
  }

  public void saveBreakTrackList(List<? extends IAudioFile> breakTrackList) {
    saveString(BREAK_TRACK_LIST_KEY, serializeFiles(breakTrackList.stream().map(IAudioFile::getSource).collect(toList())));
  }

  public List<File> loadBreakTrackList() {
    return deserializeFiles(loadString(BREAK_TRACK_LIST_KEY, ""));
  }

  public void saveEnumerationMode(EnumerationMode enumerationMode) {
    saveString(ENUMERATION_MODE_KEY, enumerationMode.name());
  }

  public EnumerationMode loadEnumerationMode(EnumerationMode defaultEnumerationModeValue) {
    return EnumerationMode.valueOf(loadString(ENUMERATION_MODE_KEY, defaultEnumerationModeValue.name()));
  }

  public void saveSoundPeriod(int soundPeriod) {
    saveInt(SOUND_PERIOD_KEY, soundPeriod);
  }

  public int loadSoundPeriod(int defaultSoundPeriod) {
    return loadInt(SOUND_PERIOD_KEY, defaultSoundPeriod);
  }

  public void saveBreakPeriod(int breakPeriod) {
    saveInt(BREAK_PERIOD_KEY, breakPeriod);
  }

  public int loadBreakPeriod(int defaultBreakPeriod) {
    return loadInt(BREAK_PERIOD_KEY, defaultBreakPeriod);
  }

  public void saveSoundPattern(String soundPattern) {
    saveString(SOUND_PATTERN_KEY, soundPattern);
  }

  public String loadSoundPattern(String defaultSoundPattern) {
    return loadString(SOUND_PATTERN_KEY, defaultSoundPattern);
  }

  public void saveBreakPattern(String breakPattern) {
    saveString(BREAK_PATTERN_KEY, breakPattern);
  }

  public String loadBreakPattern(String defaultBreakPattern) {
    return loadString(BREAK_PATTERN_KEY, defaultBreakPattern);
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

  public void saveBlendMode(BlendMode blendMode) {
    saveString(BLEND_MODE_KEY, blendMode.name());
  }

  public BlendMode loadBlendMode(BlendMode defaultBlendMode) {
   return BlendMode.valueOf(loadString(BLEND_MODE_KEY, defaultBlendMode.name()));
  }

  public void saveBlendDuration(int blendDuration) {
    saveInt(BLEND_DURATION_KEY, blendDuration);
  }

  public int loadBlendDuration(int defaultBlendDuration) {
    return loadInt(BLEND_DURATION_KEY, defaultBlendDuration);
  }

  public void saveSoundEffectOccurrences(List<SoundEffectOccurrence> soundEffectOccurrences) {
    saveString(SOUND_EFFECTS_KEY, serializeSoundEffects(soundEffectOccurrences));
  }

  public List<SoundEffectOccurrence> loadSoundEffectOccurrencesWith(SoundEffectsProvider soundEffectsProvider) {
    return deserializeSoundEffects(loadString(SOUND_EFFECTS_KEY, ""), soundEffectsProvider);
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


  //---- Helper methods

  private String serializeFiles(List<File> fileList) {
    return fileList.stream().map(File::getAbsolutePath).filter(p -> !isBlank(p)).collect(joining(File.pathSeparator));
  }

  private List<File> deserializeFiles(String fileListString) {
    return Arrays.stream(fileListString.split(File.pathSeparator)).filter(p -> !isBlank(p)).map(File::new).filter(File::exists).collect(toList());
  }

  private String serializeSoundEffects(List<SoundEffectOccurrence> soundEffectOccurrences) {
    return soundEffectOccurrences.stream().map(this::serializeSoundEffectOccurrence).collect(joining(";"));
  }

  private List<SoundEffectOccurrence> deserializeSoundEffects(String soundEffectsString, SoundEffectsProvider soundEffectsProvider) {
    return Arrays.stream(soundEffectsString.split(";")).filter(s -> !isBlank(s)).map(s -> deserializeSoundEffectOccurrence(s, soundEffectsProvider)).collect(toList());
  }

  private String serializeSoundEffectOccurrence(SoundEffectOccurrence soundEffectOccurrence) {
    return soundEffectOccurrence.getSoundEffect().getId() + ":" + soundEffectOccurrence.getTimeMillis();
  }

  private SoundEffectOccurrence deserializeSoundEffectOccurrence(String soundEffectOccurrenceString, SoundEffectsProvider soundEffectsProvider) {
    if (soundEffectOccurrenceString.contains(":")) {
      String[] parts = soundEffectOccurrenceString.split(":");
      return new SoundEffectOccurrence(soundEffectFor(parts[0], soundEffectsProvider), Long.valueOf(parts[1]));
    }
    return null;
  }

  private SoundEffect soundEffectFor(String soundEffectId, SoundEffectsProvider soundEffectsProvider) {
    return soundEffectsProvider.getSoundEffects().stream().filter(s -> s.getId().equals(soundEffectId)).findFirst().orElse(null);
  }
}
