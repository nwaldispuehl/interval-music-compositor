package ch.retorte.intervalmusiccompositor.ui.bpm;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * Specialization of the {@link BpmWindow} which works with the break list.
 */
public class BreakBpmWindow extends BpmWindow {

  public BreakBpmWindow(MessageFormatBundle messageFormatBundle, MusicListControl musicListControl, MessageProducer messageProducer, IAudioFile audioFile, int index) {
    super(messageFormatBundle, musicListControl, messageProducer, audioFile, index);
  }

  @Override
  protected void playTrack(int index) {
    getMusicListControl().playBreakTrack(index);
  }

  @Override
  protected void updateBpmWith(int index, int bpm) {
    getMusicListControl().setBreakBpm(index, bpm);
  }
}
