package ch.retorte.intervalmusiccompositor.spi.audio;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;

/**
 * Implementations know how to play music.
 * 
 * @author nw
 */
public interface MusicPlayer {

  /**
   * Starts to play the provided audio file.
   * 
   * @param audioFile the audio file to play.
   */
  void play(IAudioFile audioFile);

  /**
   * Ceases to play audio file.
   */
  void stop();
}
