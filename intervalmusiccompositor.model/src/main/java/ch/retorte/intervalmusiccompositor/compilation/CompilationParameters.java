package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;

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

  public static final BlendMode DEFAULT_BLEND_MODE = SEPARATE;
  public static final Double DEFAULT_BLEND_TIME = 1d;
  public static final EnumerationMode DEFAULT_ENUMERATION_MODE = SINGLE_EXTRACT;
  public static final ListSortMode DEFAULT_LIST_SORT_MODE = SORT;

  public List<Integer> musicPattern = new ArrayList<>();
  public List<Integer> breakPattern = new ArrayList<>();
  public int iterations;
  public BlendMode blendMode = DEFAULT_BLEND_MODE;
  public double blendTime = DEFAULT_BLEND_TIME;
  public EnumerationMode enumerationMode = DEFAULT_ENUMERATION_MODE;
  public ListSortMode listSortMode = DEFAULT_LIST_SORT_MODE;
  public String outputPath;
  public String encoderIdentifier;

}
