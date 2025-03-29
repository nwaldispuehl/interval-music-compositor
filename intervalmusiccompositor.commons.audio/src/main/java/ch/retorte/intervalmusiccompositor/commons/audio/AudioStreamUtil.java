package ch.retorte.intervalmusiccompositor.commons.audio;

import org.tritonus.sampled.file.WaveAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helps with recurring tasks around audio.
 */
public class AudioStreamUtil {

  // ---- Methods

  /**
   * Determines the length of a {@link InputStream} in milliseconds. For better precision we represent them as double.
   */
  public double lengthInMillisecondsOf(InputStream inputStream) {
    AudioInputStream audioInputStream = audioInputStreamFromWaveInputStream(inputStream);
    return getStreamLengthInMillisecondsOf(audioInputStream);
  }

  /**
   * Converts the provided {@link InputStream} to a {@link AudioInputStream} but converts any checked exception to a {@link RuntimeException}.
   */
  private AudioInputStream audioInputStreamFromWaveInputStream(InputStream inputStream) {
    try {
      return audioInputStreamFrom(inputStream);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts the provided {@link InputStream} to a {@link AudioInputStream}.
   */
  public AudioInputStream audioInputStreamFrom(InputStream inputStream) throws IOException, UnsupportedAudioFileException {
    return new WaveAudioFileReader().getAudioInputStream(inputStream);
  }

  /**
   * Determines the length of a {@link AudioInputStream} in milliseconds. For better precision we represent them as double.
   */
  private double getStreamLengthInMillisecondsOf(AudioInputStream audioInputStream) {
    return audioInputStream.getFrameLength() * 1000 / audioInputStream.getFormat().getFrameRate();
  }
}
