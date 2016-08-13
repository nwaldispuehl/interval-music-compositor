package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.list.BlendMode.SEPARATE;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SORT;

/**
 * Aggregates all needed compilation parameters.
 * 
 * @author nw
 */
public class CompilationParameters {

  //---- Static

  public static final BlendMode DEFAULT_BLEND_MODE = SEPARATE;
  public static final Double DEFAULT_BLEND_DURATION = 1d;
  public static final EnumerationMode DEFAULT_ENUMERATION_MODE = SINGLE_EXTRACT;
  public static final ListSortMode DEFAULT_LIST_SORT_MODE = SORT;

  private static final String PATTERN_SEPARATOR = ",";

  //---- Fields

  private List<Integer> musicPattern = new ArrayList<>();
  private List<Integer> breakPattern = new ArrayList<>();
  private int iterations;
  private BlendMode blendMode = DEFAULT_BLEND_MODE;
  private double blendDuration = DEFAULT_BLEND_DURATION;
  private EnumerationMode enumerationMode = DEFAULT_ENUMERATION_MODE;
  private ListSortMode listSortMode = DEFAULT_LIST_SORT_MODE;
  private String outputPath;
  private String encoderIdentifier;


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
    List<Integer> result = Lists.newArrayList();
    for(String s : Splitter.on(PATTERN_SEPARATOR).trimResults().omitEmptyStrings().split(musicPattern)) {
      try {
        result.add(Math.abs(Integer.parseInt(s)));
      }
      catch (Exception e) {
        // ignoring unsuitable values
      }
    }
    return result;
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

  public void setEnumerationMode(EnumerationMode enumerationMode) {
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
      if (0 < breakPattern.size()) {
        singleIterationDuration += breakPattern.get(i % breakPattern.size());
      }
    }

    int durationEstimationValue = singleIterationDuration * iterations;

    // If cross fading is activated, add fade interval once (= a half at start
    // and one at the end)
    if (blendMode == BlendMode.CROSS && 0 < singleIterationDuration && 0 < iterations) {
      durationEstimationValue += blendDuration;
    }
    return durationEstimationValue;
  }

}
