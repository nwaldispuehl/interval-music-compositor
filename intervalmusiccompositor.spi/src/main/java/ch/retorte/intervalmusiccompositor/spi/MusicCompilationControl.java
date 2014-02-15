package ch.retorte.intervalmusiccompositor.spi;

import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;

/**
 * Offers means for controlling the compilation..
 * 
 * @author nw
 */
public interface MusicCompilationControl {

  void startCompilation(CompilationParameters compilationParameters);
}
