package ch.retorte.intervalmusiccompositor.ui.audiofilelist;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.Utf8Control;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.ui.bpm.BpmWindow;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ResourceBundle;

import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;

/**
 * Custom trackList cell for {@link IAudioFile}.
 */
class AudioFileListCell extends ListCell<IAudioFile> {

  //---- Fields

  private ResourceBundle resourceBundle = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME, new Utf8Control());

  private AudioFileListCellController listCellController;
  private MessageFormatBundle messageFormatBundle;
  private MusicListControl musicListControl;
  private MessageProducer messageProducer;


  //---- Constructor

  AudioFileListCell(MessageFormatBundle messageFormatBundle, MusicListControl musicListControl, MessageProducer messageProducer) {
    this.messageFormatBundle = messageFormatBundle;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;
    listCellController = new AudioFileListCellController();

    itemProperty().addListener((observable, oldAudioFile, newAudioFile) -> {
      if (newAudioFile != null) {
        updateWith(newAudioFile);
      }
      else {
        setGraphic(null);
      }
    });

    emptyProperty().addListener((observable, wasEmpty, isEmpty) -> {
      if (isEmpty) {
        setGraphic(null);
      }
      else {
        setGraphic(listCellController);
      }
    });

    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
  }


  //---- Methods

  private DraggableAudioFileListView listView() {
    return (DraggableAudioFileListView) getListView();
  }

  private void updateWith(IAudioFile audioFile) {
    listCellController.updateWith(getOrdinalNumberFor(audioFile), audioFile);
  }

  protected BpmWindow createBpmWindowFrom(MessageFormatBundle messageFormatBundle, MusicListControl musicListControl, MessageProducer messageProducer, IAudioFile audioFile, int index) {
    return new BpmWindow(messageFormatBundle, musicListControl, messageProducer, audioFile, index);
  }

  private int getOrdinalNumberFor(IAudioFile audioFile) {
    return getListView().getItems().indexOf(audioFile);
  }


  //---- Inner class

  private class AudioFileListCellController extends HBox {

    //---- Static

    private static final String LIST_CELL_LAYOUT_FILE = "/layouts/AudioFileListCell.fxml";

    private static final String ORANGE = "#D7AD04";
    private static final String BLUE = "#2F60AB";
    private static final String GREEN = "#5BA853";


    //---- Fields

    private SimpleBooleanProperty isBpmSupported = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty isReady = new SimpleBooleanProperty(false);

    private Image processing = new Image(getClass().getResource("/images/tracklist/processing_icon.gif").toString());
    private Image ok = new Image(getClass().getResource("/images/tracklist/music_icon.png").toString());
    private Image error = new Image(getClass().getResource("/images/tracklist/error_icon.png").toString());
    private Image warning = new Image(getClass().getResource("/images/tracklist/tooshort_icon.png").toString());
    private Image queued = new Image(getClass().getResource("/images/tracklist/queued_icon.png").toString());

    @FXML
    ImageView imageView;

    @FXML
    Text track;

    @FXML
    Text duration;

    @FXML
    Text bpm;

    @FXML
    Text title;

    @FXML
    Text status;


    //---- Constructor

    AudioFileListCellController() {
      loadFXML();
    }


    //---- Methods

    private void loadFXML() {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LIST_CELL_LAYOUT_FILE), resourceBundle);
      fxmlLoader.setRoot(this);
      fxmlLoader.setController(this);

      try {
        fxmlLoader.load();

      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }
    }

    void updateWith(int ordinalNumber, IAudioFile audioFile) {
      setTrackWith(ordinalNumber);
      setDurationWith(audioFile);
      setBpmWith(audioFile);
      setTitleWith(audioFile);
      setStatusWith(audioFile);
      setContextMenuWith(audioFile);
    }

    private void setTrackWith(int ordinalNumber) {
      track.setText(String.valueOf(ordinalNumber + 1));
    }

    private void setDurationWith(IAudioFile audioFile) {
      if (0 < audioFile.getDuration()) {
        duration.setText(format(audioFile.getDuration()));
      }
      else {
        duration.setText(null);
      }
    }

    private void setBpmWith(IAudioFile audioFile) {
      if (!audioFile.hasBpm()) {
        bpm.setText("");
        return;
      }

      this.bpm.setText(String.valueOf(audioFile.getBpm()) + " bpm");
      Color bpmColor = Color.web(ORANGE);

      if (audioFile.isBpmReliable()) {
        bpmColor = Color.web(GREEN);
      }

      if (audioFile.isBpmStored()) {
        bpmColor = Color.web(BLUE);
      }

      this.bpm.setFill(bpmColor);
    }

    private void setTitleWith(IAudioFile audioFile) {
      title.setText(audioFile.getDisplayName());
    }

    private void setStatusWith(IAudioFile audioFile) {
      switch (audioFile.getStatus()) {
        case IN_PROGRESS:
          imageView.setImage(processing);
          status.setText(resourceBundle.getString("ui.form.music_list.loading_text"));
          return;
        case ERROR:
          imageView.setImage(error);
          status.setText(audioFile.getErrorMessage());
          return;
        case OK:
          imageView.setImage(ok);
          status.setText(audioFile.getSource().getParentFile().getAbsolutePath());
          return;
        case QUEUED:
          imageView.setImage(queued);
          status.setText(resourceBundle.getString("ui.form.music_list.queuing_text"));
          return;
        default:
          imageView.setImage(warning);
      }
    }

    private void setContextMenuWith(IAudioFile audioFile) {
      setContextMenu(createContextMenu());
      registerMenuStateListenerWith(audioFile);
      setMenuStateWith(audioFile);
    }

    private void registerMenuStateListenerWith(IAudioFile audioFile) {
      audioFile.addChangeListener(updatedAudioFile -> {
        isBpmSupported.setValue(updatedAudioFile.isBpmSupported());
        isReady.setValue(updatedAudioFile.isOK());
      });
    }

    private void setMenuStateWith(IAudioFile audioFile) {
      isBpmSupported.set(audioFile.isBpmSupported());
      isReady.set(audioFile.isOK());
    }

    private String format(long milliSeconds) {
      return new FormatTime().getStrictFormattedTime(milliSeconds / 1000.0);
    }

    private ContextMenu createContextMenu() {
      ContextMenu contextMenu = new ContextMenu();

      contextMenu.getItems().add(createChangeBpmItem());
      contextMenu.getItems().add(createWriteBpmItem());
      contextMenu.getItems().add(separator());
      contextMenu.getItems().add(createDeleteItem());

      return contextMenu;
    }

    private MenuItem createChangeBpmItem() {
      MenuItem changeBpmItem = new MenuItem();
      changeBpmItem.setText(resourceBundle.getString("ui.list_context.change_bpm"));
      changeBpmItem.setOnAction(event -> {
        BpmWindow bpmWindow = createBpmWindowFrom(messageFormatBundle, musicListControl, messageProducer, getItem(), getIndex());
        bpmWindow.show();
      });
      changeBpmItem.disableProperty().bind(isBpmSupported.not().or(isReady.not()));
      return changeBpmItem;
    }

    private MenuItem createWriteBpmItem() {
      MenuItem writeBpmItem = new MenuItem();
      writeBpmItem.setText(resourceBundle.getString("ui.list_context.write_bpm"));
      writeBpmItem.setOnAction(event -> listView().writeBpmFor(getItem()));
      writeBpmItem.disableProperty().bind(isBpmSupported.not().or(isReady.not()));
      return writeBpmItem;
    }

    private MenuItem separator() {
      return new SeparatorMenuItem();
    }

    private MenuItem createDeleteItem() {
      MenuItem deleteItem = new MenuItem();
      deleteItem.setText(resourceBundle.getString("ui.list_context.remove_track_sg"));
      deleteItem.setOnAction(event -> listView().removeTrack(getItem()));

      return deleteItem;
    }

  }

}
