package ch.retorte.intervalmusiccompositor.model.compilation;

import ch.retorte.intervalmusiccompositor.model.list.BlendMode;
import ch.retorte.intervalmusiccompositor.model.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.model.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Aggregates all needed compilation parameters.
 */
public class CompilationParameters {

  //---- Static

  public static final BlendMode DEFAULT_BLEND_MODE = BlendMode.SEPARATE;
  public static final Double DEFAULT_BLEND_DURATION = 1d;
  public static final EnumerationMode DEFAULT_ENUMERATION_MODE = EnumerationMode.SINGLE_EXTRACT;
  public static final ListSortMode DEFAULT_LIST_SORT_MODE = ListSortMode.SORT;
  private static final double DEFAULT_BREAK_VOLUME = 1;

  private static final String PATTERN_SEPARATOR = ",";


  //---- Fields

  private final List<Integer> musicPattern = new ArrayList<>();
  private final List<Integer> breakPattern = new ArrayList<>();
  private final List<SoundEffectOccurrence> soundEffectOccurrences = new ArrayList<>();
  private int iterations;
  private BlendMode blendMode = DEFAULT_BLEND_MODE;
  private double blendDuration = DEFAULT_BLEND_DURATION;
  private EnumerationMode enumerationMode = DEFAULT_ENUMERATION_MODE;
  private ListSortMode listSortMode = DEFAULT_LIST_SORT_MODE;
  private String outputPath;
  private String encoderIdentifier;
  private String defaultOutputPath;
  private double breakVolume = DEFAULT_BREAK_VOLUME;


  //---- Methods

  public void setMusicPattern(String pattern) {
    musicPattern.clear();
    musicPattern.addAll(parsePattern(pattern));
  }

  public void setMusicPattern(Integer period) {
    musicPattern.clear();
    musicPattern.add(period);
  }


  public List<Integer> getMusicPattern() {
    return musicPattern;
  }

  public void setBreakPattern(String pattern) {
    breakPattern.clear();
    breakPattern.addAll(parsePattern(pattern));
  }

  public void setBreakPattern(Integer period) {
    breakPattern.clear();
    breakPattern.add(period);
  }

  public List<Integer> getBreakPattern() {
    return breakPattern;
  }

  private List<Integer> parsePattern(String musicPattern) {
    List<Integer> result = new ArrayList<>();
    for(String s : split(musicPattern)) {
      try {
        result.add(Math.abs(Integer.parseInt(s)));
      }
      catch (Exception e) {
        // ignoring unsuitable values
      }
    }
    return result;
  }

  private List<String> split(String musicPattern) {
    return stream(musicPattern.split(PATTERN_SEPARATOR)).map(String::trim).filter(this::notEmpty).collect(toList());
  }

  private boolean notEmpty(String s) {
    return !isEmpty(s);
  }

  private boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public void addSoundEffectOccurrence(SoundEffectOccurrence soundEffectOccurrence) {
    soundEffectOccurrences.add(soundEffectOccurrence);
  }

  public void removeSoundEffectOccurrence(SoundEffectOccurrence soundEffectOccurrence) {
    soundEffectOccurrences.remove(soundEffectOccurrence);
  }

  public List<SoundEffectOccurrence> getSoundEffectOccurrences() {
    return soundEffectOccurrences;
  }

  private void setBlendMode(BlendMode blendMode) {
    this.blendMode = blendMode;
  }

  public void setBlendMode(String blendMode) {
    try {
      setBlendMode(BlendMode.valueOf(blendMode));
    }
    catch (Exception e) {
      // nop
    }
  }

  public BlendMode getBlendMode() {
    return blendMode;
  }

  public void setBlendDuration(Double blendDuration) {
    this.blendDuration = blendDuration;
  }

  public Double getBlendDuration() {
    return blendDuration;
  }

  private void setEnumerationMode(EnumerationMode enumerationMode) {
    this.enumerationMode = enumerationMode;
  }

  public void setEnumerationMode(String enumerationMode) {
    try {
      setEnumerationMode(EnumerationMode.valueOf(enumerationMode));
    }
    catch (Exception e) {
      // nop
    }
  }

  public EnumerationMode getEnumerationMode() {
    return enumerationMode;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public void setDefaultOutputPath(String defaultOutputPath) {
    this.defaultOutputPath = defaultOutputPath;
  }

  public void resetOutputPath() {
    outputPath = defaultOutputPath;
  }

  public String getEncoderIdentifier() {
    return encoderIdentifier;
  }

  public void setEncoderIdentifier(String encoderIdentifier) {
    this.encoderIdentifier = encoderIdentifier;
  }

  public void setIterations(Integer iterations) {
    this.iterations = Math.abs(iterations);
  }

  public Integer getIterations() {
    return iterations;
  }

  public ListSortMode getListSortMode() {
    return listSortMode;
  }

  public void setListSortMode(ListSortMode listSortMode) {
    this.listSortMode = listSortMode;
  }

  /**
   * A set of parameters is considered usable if there is something to record, i.e. the duration is not zero.
   */
  public boolean hasUsableData() {
    return 0 < getDurationEstimationSeconds();
  }

  public int getDurationEstimationSeconds() {
    int singleIterationDuration = 0;

    for (int i = 0; i < musicPattern.size(); i++) {

      // Add current pattern item
      singleIterationDuration += musicPattern.get(i);

      // Add current break item (or restart if there are not enough of them)
      if (!breakPattern.isEmpty()) {
        singleIterationDuration += breakPattern.get(i % breakPattern.size());
      }
    }

    int durationEstimationValue = singleIterationDuration * iterations;

    // If cross fading is activated, add fade interval once (= a half at start
    // and one at the end)
    if (blendMode == BlendMode.CROSS && 0 < singleIterationDuration && 0 < iterations) {
      durationEstimationValue += (int) blendDuration;
    }
    return durationEstimationValue;
  }

  public boolean hasBreakPattern() {
    return breakPattern.stream().anyMatch(i -> 0 < i);
  }

  public int getShortestDurationPairFromPattern() {
    List<Integer> musicPattern = getMusicPattern();
    List<Integer> breakPattern = getBreakPattern();

    int smallestPatternPair = Integer.MAX_VALUE;
    for (int i = 0; i < musicPattern.size(); i++) {
      int m = musicPattern.get(i);
      int b = 0;
      if (!breakPattern.isEmpty()) {
        b = breakPattern.get(i % breakPattern.size());
      }

      if (m + b < smallestPatternPair) {
        smallestPatternPair = m + b;
      }
    }
    return smallestPatternPair;
  }

  public void setBreakVolume(double breakVolume) {
    this.breakVolume = breakVolume;
  }

  public double getBreakVolume() {
    return breakVolume;
  }
}
