package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates all needed compilation parameters.
 * 
 * @author nw
 */
public class CompilationParameters {

  public List<Integer> musicPattern = new ArrayList<>();
  public List<Integer> breakPattern = new ArrayList<>();
  public int iterations;
  public BlendMode blendMode = BlendMode.SEPARATE;
  public double blendTime;
  public EnumerationMode enumerationMode = EnumerationMode.SINGLE_EXTRACT;
  public ListSortMode listSortMode = ListSortMode.SORT;
  public String outputPath;
  public String encoderIdentifier;

}
