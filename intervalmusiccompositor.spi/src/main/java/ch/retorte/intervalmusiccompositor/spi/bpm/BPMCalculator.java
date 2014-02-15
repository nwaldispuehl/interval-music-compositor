package ch.retorte.intervalmusiccompositor.spi.bpm;

import javax.sound.sampled.AudioInputStream;

/**
 * @author nw
 */
public interface BPMCalculator {

  /**
   * Examines the provided {@link AudioInputStream} and calculates the BPM value.
   * 
   * @param audioInputStream
   *          the audio sample to be inspected for BPM calculation.
   * @return a BPM value, or null, if the calculator was not able to detect one.
   */
  Integer calculateBPM(AudioInputStream audioInputStream);

}
