package ch.retorte.intervalmusiccompositor.ui.audiofilelist;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.model.list.ObservableList;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.util.ChangeListener;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import javafx.collections.FXCollections;
import javafx.event.EventTarget;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;


/**
 * Draggable audio file trackList view supports drag and drop and shows the {@link IAudioFile}.
 */
public class DraggableAudioFileListView extends ListView<IAudioFile> {

  private MessageProducer messageProducer;
  private MusicListControl musicListControl;
  private MessageFormatBundle messageFormatBundle;

  private final Collection<ChangeListener<List<IAudioFile>>> listChangeListeners = newArrayList();

  private ChangeListener<IAudioFile> audioFileStateChangeListener;

  public void initializeWith(ObservableList<IAudioFile> items, MessageFormatBundle messageFormatBundle, MessageProducer messageProducer, MusicListControl musicListControl, ChangeListener<IAudioFile> audioFileStateChangeListener) {

    // To decouple the JavaFX part, we use an independent observable list in the other packages, and couple them here.
    javafx.collections.ObservableList<IAudioFile> observableItems = FXCollections.observableArrayList(items);

    items.addListener(c -> {
      observableItems.clear();
      observableItems.addAll(items);
    });

    setItems(observableItems);

    this.messageFormatBundle = messageFormatBundle;
    this.messageProducer = messageProducer;
    this.musicListControl = musicListControl;

    this.audioFileStateChangeListener = audioFileStateChangeListener;

    initializeSelection();
    initializeDragNDrop();
    initializeCellFactory();
    initializeKeyActions();
  }

  public void addListChangeListener(ChangeListener<List<IAudioFile>> changeListener) {
    listChangeListeners.add(changeListener);
  }

  void notifyListChangeListeners() {
    listChangeListeners.forEach(cl -> cl.changed(getItems()));
  }

  private void initializeSelection() {
    getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
  }

  private void initializeDragNDrop() {

    setOnDragOver(event -> {
      messageProducer.send(new DebugMessage(this, "Drag over detected."));

      if (isValid(event.getDragboard())) {
        messageProducer.send(new DebugMessage(this, "Dragboard is valid. Content types: " + event.getDragboard().getContentTypes()));
        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      }
      else {
        messageProducer.send(new DebugMessage(this, "Dragboard is not valid. Content types: " + event.getDragboard().getContentTypes()));
      }
      event.consume();
    });

    setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;

      if (isValid(db)) {
        List<File> files = db.getFiles();
        if (files != null) {
          files.forEach(this::addTrack);
          success = true;
        }
      }

      event.setDropCompleted(success);
      event.consume();
    });
  }

  private boolean isValid(Dragboard dragboard) {
    return dragboard.hasFiles();
  }

  protected void initializeCellFactory() {
    setCellFactory(items -> new AudioFileListCell(getMessageFormatBundle(), getMusicListControl(), getMessageProducer()));
  }

  protected MessageProducer getMessageProducer() {
    return messageProducer;
  }

  MessageFormatBundle getMessageFormatBundle() {
    return messageFormatBundle;
  }


  private void initializeKeyActions() {
    addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (isDeleteCode(event.getCode())) {
        EventTarget target = event.getTarget();
        if (target instanceof DraggableAudioFileListView) {
          DraggableAudioFileListView listView = (DraggableAudioFileListView) target;
          removeTracks(newArrayList(listView.getSelectionModel().getSelectedIndices()));
        }
      }
    });
  }

  private boolean isDeleteCode(KeyCode keyCode) {
    return keyCode == KeyCode.DELETE || keyCode == KeyCode.BACK_SPACE;
  }

  public IAudioFile addTrack(File file) {
    IAudioFile newTrack = musicListControl.appendMusicTrack(file);
    addChangeListenersTo(newTrack);
    notifyListChangeListeners();
    return newTrack;
  }

  public void removeTrack(IAudioFile audioFile) {
    getMusicListControl().removeMusicTrack(indexOf(audioFile));
    notifyListChangeListeners();
  }

  int indexOf(IAudioFile audioFile) {
    return trackList().indexOf(audioFile);
  }

  protected List<IAudioFile> trackList() {
    return getMusicListControl().getMusicList();
  }

  public void removeTracks(List<Integer> indices) {
    getMusicListControl().removeMusicTracks(indices.stream().mapToInt(i->i).toArray());
    notifyListChangeListeners();
  }

  void addChangeListenersTo(IAudioFile newTrack) {
    if (newTrack != null) {
      newTrack.addChangeListener(newStatus -> refresh());

      if (audioFileStateChangeListener != null) {
        newTrack.addChangeListener(audioFileStateChangeListener);
      }
    }
  }

  MusicListControl getMusicListControl() {
    return musicListControl;
  }

  public void writeBpmFor(IAudioFile audioFile) {
    getMusicListControl().writeMusicBpm(indexOf(audioFile));
  }

}
