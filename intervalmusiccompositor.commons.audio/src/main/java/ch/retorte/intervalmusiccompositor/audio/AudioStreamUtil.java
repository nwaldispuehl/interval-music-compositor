package ch.retorte.intervalmusiccompositor.audio;

import org.tritonus.sampled.file.WaveAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Helps with recurring tasks around audio.
 */
public class AudioStreamUtil {

  public AudioInputStream streamFromWaveResource(String resource) {
    try {
      InputStream inputStream = getInputStreamFromResource(resource);
      return audioInputStreamFrom(inputStream);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public AudioInputStream audioInputStreamFrom(InputStream inputStream) throws IOException, UnsupportedAudioFileException {
    return new WaveAudioFileReader().getAudioInputStream(inputStream);
  }

  /**
   * Converts a given resource path to a file.
   */
  public InputStream getInputStreamFromResource(String resourceFilePath) throws URISyntaxException {
    return getClass().getResourceAsStream(resourceFilePath);
  }

  /**
   * Determines the length of a {@link AudioInputStream}.
   */
  public long getStreamLengthInMilliSecondsOf(AudioInputStream inputStream) {
    return (long) ((inputStream.getFrameLength() / inputStream.getFormat().getFrameRate()) * 1000);
  }

  public long lengthInMillisOf(String resource) {
    AudioInputStream audioInputStream = streamFromWaveResource(resource);
    return getStreamLengthInMilliSecondsOf(audioInputStream);
  }
}
