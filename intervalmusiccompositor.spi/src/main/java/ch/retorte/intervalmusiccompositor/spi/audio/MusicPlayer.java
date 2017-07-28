package ch.retorte.intervalmusiccompositor.spi.audio;

import javax.sound.sampled.AudioInputStream;

/**
 * Implementations know how to play music.
 * 
 * @author nw
 */
public interface MusicPlayer {

  /**
   * Starts to play the provided audio input stream.
   * 
   * @param audioInputStream the audio input stream to play.
   */
  void play(AudioInputStream audioInputStream);

  /**
   * Ceases to play audio file.
   */
  void stop();
}
