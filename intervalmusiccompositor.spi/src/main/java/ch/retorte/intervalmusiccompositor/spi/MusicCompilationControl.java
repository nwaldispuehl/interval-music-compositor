package ch.retorte.intervalmusiccompositor.spi;

import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;

import java.util.List;

/**
 * Offers means for controlling the compilation..
 * 
 * @author nw
 */
public interface MusicCompilationControl {

  void startCompilation(CompilationParameters compilationParameters);

  /**
   *  Provides all available decoders.
   */
  List<AudioFileDecoder> getAvailableDecoders();

  /**
   *  Provides all encoders which are valid in this environment.
   */
  List<AudioFileEncoder> getAvailableEncoders();
}
