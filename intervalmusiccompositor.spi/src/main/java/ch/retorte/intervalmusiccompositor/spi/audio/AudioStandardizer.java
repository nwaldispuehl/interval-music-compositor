package ch.retorte.intervalmusiccompositor.spi.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * Normalizes an {@link AudioInputStream} so that it has standardized audio properties.
 */
public interface AudioStandardizer {

  // ---- Statics

  float SAMPLE_RATE = 44100.0F;
  AudioFormat.Encoding TARGET_ENCODING = AudioFormat.Encoding.PCM_SIGNED;
  AudioFormat TARGET_AUDIO_FORMAT = new AudioFormat(TARGET_ENCODING, SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false);


  // ---- Methods

  /**
   * Standardizes the input stream to our desired format.
   * 
   * @param audioInputStream
   *          the to be standardized source stream.
   * @return the standardized stream.
   */
  AudioInputStream standardize(AudioInputStream audioInputStream);
}
