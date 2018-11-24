package ch.retorte.intervalmusiccompositor.spi.audio;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;

/**
 * Can play back an {@link IAudioFile}.
 */
public interface AudioFileMusicPlayer extends MusicPlayer {

  /**
   * Starts to play the provided audio file.
   * 
   * @param audioFile the audio file to play.
   */
  void play(IAudioFile audioFile);

}
