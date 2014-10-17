package ch.retorte.intervalmusiccompositor.spi.audio;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

/**
 * Converts interesting data structures to byte arrays.
 *
 * @author nw
 */
public interface ByteArrayConverter {

  /**
   * Converts an AudioInputStream to a byte array.
   */
  byte[] convert(AudioInputStream audioInputStream) throws IOException;

}
