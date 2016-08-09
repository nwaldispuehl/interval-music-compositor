package ch.retorte.intervalmusiccompositor.ui.audiofilelist;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import com.google.common.primitives.Ints;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;

/**
 * Specialization of the music trackList which applies everything to the break trackList.
 */
public class DraggableAudioFileBreakListView extends DraggableAudioFileListView {

  @Override
  public void initializeWith(ObservableList<IAudioFile> items, MessageFormatBundle bundle, MessageProducer messageProducer, MusicListControl musicListControl) {
    super.initializeWith(items, bundle, messageProducer, musicListControl);
  }

  @Override
  public void addTrack(File file) {
    if (!trackList().isEmpty()) {
      getMusicListControl().removeBreakTrack(0);
    }

    IAudioFile newTrack = getMusicListControl().appendBreakTrack(file);
    addChangeListenerTo(newTrack);
    notifyListChangeListeners();
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
    getMusicListControl().removeMusicTracks(Ints.toArray(indices));
    notifyListChangeListeners();
  }

  @Override
  public void writeBpmFor(IAudioFile audioFile) {
    getMusicListControl().writeBreakBpm(indexOf(audioFile));
  }
}
