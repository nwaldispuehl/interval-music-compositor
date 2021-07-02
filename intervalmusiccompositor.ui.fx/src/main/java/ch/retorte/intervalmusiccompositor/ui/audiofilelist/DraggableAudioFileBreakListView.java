package ch.retorte.intervalmusiccompositor.ui.audiofilelist;

import ch.retorte.intervalmusiccompositor.model.util.ChangeListener;
import ch.retorte.intervalmusiccompositor.model.list.ObservableList;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;


import java.io.File;
import java.util.List;

/**
 * Specialization of the music trackList which applies everything to the break trackList.
 */
public class DraggableAudioFileBreakListView extends DraggableAudioFileListView {

  @Override
  public void initializeWith(ObservableList<IAudioFile> items, MessageFormatBundle bundle, MessageProducer messageProducer, MusicListControl musicListControl, ChangeListener<IAudioFile> audioFileStateChangeListener) {
    super.initializeWith(items, bundle, messageProducer, musicListControl, audioFileStateChangeListener);
  }

  @Override
  public IAudioFile addTrack(File file) {
    if (!trackList().isEmpty()) {
      getMusicListControl().removeBreakTrack(0);
    }

    IAudioFile newTrack = getMusicListControl().appendBreakTrack(file);
    addChangeListenersTo(newTrack);
    notifyListChangeListeners();
    return newTrack;
  }

  @Override
  protected void initializeCellFactory() {
    setCellFactory(items -> new AudioFileBreakListCell(getMessageFormatBundle(), getMusicListControl(), getMessageProducer()));
  }

  @Override
  public void removeTrack(IAudioFile audioFile) {
    getMusicListControl().removeBreakTrack(indexOf(audioFile));
    notifyListChangeListeners();
  }

  @Override
  protected ObservableList<IAudioFile> trackList() {
    return getMusicListControl().getBreakList();
  }

  @Override
  public void removeTracks(List<Integer> indices) {
    getMusicListControl().removeMusicTracks(indices.stream().mapToInt(i->i).toArray());
    notifyListChangeListeners();
  }

  @Override
  public void writeBpmFor(IAudioFile audioFile) {
    getMusicListControl().writeBreakBpm(indexOf(audioFile));
  }
}
