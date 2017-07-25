package ch.retorte.intervalmusiccompositor.audio;

import org.tritonus.sampled.file.WaveAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Helps with recurring tasks around audio.
 */
public class AudioStreamUtil {

  public AudioInputStream streamFromWaveResource(String resource) {
    try {
      InputStream inputStream = getInputStreamFromResource(resource);
      return new WaveAudioFileReader().getAudioInputStream(inputStream);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts a given resource path to a file.
   */
  public InputStream getInputStreamFromResource(String resourceFilePath) throws URISyntaxException {
    return getClass().getResourceAsStream(resourceFilePath);
  }
}
