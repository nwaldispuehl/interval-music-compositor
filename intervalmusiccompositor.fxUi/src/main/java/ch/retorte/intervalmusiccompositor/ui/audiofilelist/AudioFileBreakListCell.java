package ch.retorte.intervalmusiccompositor.ui.audiofilelist;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.ui.bpm.BpmWindow;
import ch.retorte.intervalmusiccompositor.ui.bpm.BreakBpmWindow;

/**
 * Specialization of the {@link AudioFileListCell} suitable for the break list.
 */
class AudioFileBreakListCell extends AudioFileListCell {

  AudioFileBreakListCell(MessageFormatBundle messageFormatBundle, MusicListControl musicListControl, MessageProducer messageProducer) {
    super(messageFormatBundle, musicListControl, messageProducer);
  }

  @Override
  protected BpmWindow createBpmWindowFrom(MessageFormatBundle messageFormatBundle, MusicListControl musicListControl, MessageProducer messageProducer, IAudioFile audioFile, int index) {
    return new BreakBpmWindow(messageFormatBundle, musicListControl, messageProducer, audioFile, index);
  }
}
