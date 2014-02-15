package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;

import java.util.List;

/**
 * Aggregates all needed compilation parameters.
 * 
 * @author nw
 */
public class CompilationParameters {

  public List<Integer> musicPattern;
  public List<Integer> breakPattern;
  public int iterations;
  public BlendMode blendMode;
  public double blendTime;
  public EnumerationMode enumerationMode;
  public ListSortMode listSortMode;
  public String outputPath;

  public static CompilationParameters buildFrom(List<Integer> musicPattern, List<Integer> breakPattern, int iterations, BlendMode blendMode, double blendTime,
      ListSortMode listSortMode, String outputPath, EnumerationMode enumerationMode) {
    CompilationParameters result = new CompilationParameters();

    result.musicPattern = musicPattern;
    result.breakPattern = breakPattern;
    result.iterations = iterations;
    result.blendMode = blendMode;
    result.blendTime = blendTime;
    result.listSortMode = listSortMode;
    result.outputPath = outputPath;
    result.enumerationMode = enumerationMode;

    return result;
  }
}
