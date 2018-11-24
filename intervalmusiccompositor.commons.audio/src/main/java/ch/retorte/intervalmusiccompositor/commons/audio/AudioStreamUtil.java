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



  public AudioInputStream audioInputStreamFrom(InputStream inputStream) throws IOException, UnsupportedAudioFileException {
    return new WaveAudioFileReader().getAudioInputStream(inputStream);
  }

  /**
   * Determines the length of a {@link AudioInputStream}.
   */
  private long getStreamLengthInMilliSecondsOf(AudioInputStream audioInputStream) {
    return (long) ((audioInputStream.getFrameLength() / audioInputStream.getFormat().getFrameRate()) * 1000);
  }

  public long lengthInMillisOf(InputStream inputStream) {
    AudioInputStream audioInputStream = audioInputStreamFromWaveInputStream(inputStream);
    return getStreamLengthInMilliSecondsOf(audioInputStream);
  }

  private AudioInputStream audioInputStreamFromWaveInputStream(InputStream inputStream) {
    try {
      return audioInputStreamFrom(inputStream);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
