package ch.retorte.intervalmusiccompositor.ui.bpm;

import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Allows to pre-hear a track and determine its BPM (beats per minute) via tapping.
 */
public class BpmWindow {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/BpmWindow.fxml";

  //---- FX Widgets

  @FXML
  private Button playButton;

  @FXML
  private Button stopButton;

  @FXML
  private Label bpm;

  @FXML
  private Button tapButton;

  @FXML
  private Spinner<Integer> selectedBPMSpinner;

  @FXML
  private Button cancelButton;

  @FXML
  private Button okButton;

  //---- Fields

  private Parent parent;
  private final MessageFormatBundle messageFormatBundle;
  private final MusicListControl musicListControl;
  private MessageProducer messageProducer;
  private final IAudioFile audioFile;
  private final int index;

  private SimpleIntegerProperty tappedBpm = new SimpleIntegerProperty(0);
  private SimpleStringProperty tappedBpmString = new SimpleStringProperty();
  private SimpleIntegerProperty selectedBpm = new SimpleIntegerProperty(0);

  private Tap tap;

  //---- Constructor

  public BpmWindow(MessageFormatBundle messageFormatBundle, MusicListControl musicListControl, MessageProducer messageProducer, IAudioFile audioFile, int index) {
    this.messageFormatBundle = messageFormatBundle;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;
    this.audioFile = audioFile;
    this.index = index;

    loadFXML();
    initialize();
  }


  //---- Methods

  private void loadFXML() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LAYOUT_FILE), messageFormatBundle.getBundle());
    fxmlLoader.setController(this);

    try {
      parent = fxmlLoader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public void show() {
    Stage stage = new Stage();
    stage.setTitle(messageFormatBundle.getString("ui.determine_bpm.title"));
    stage.setScene(new Scene(parent));
    stage.setResizable(true);
    stage.show();
    Platform.runLater(() -> {
      /* We do these little tricks to adapt the window size to the stages content. */
      stage.sizeToScene();
      stage.setMinWidth(stage.getWidth());
      stage.setMinHeight(stage.getHeight());
    });

    // We catch also the click on the upper right 'x' with this.
    stage.setOnCloseRequest(event -> close());
  }

  private void initialize() {
    initializeMusicControl();
    initializeBpmIndicator();
    initializeTap();
    initializeTapButton();
    initializeSelectedBpmSpinner();
    initializeButtons();
    initializeFieldsWithExistingBpmValue();
  }

  private void initializeMusicControl() {
    playButton.setOnAction(event -> {
      playTrack(index);
      addDebugMessage("Pressed playback button for track " + audioFile.getDisplayName());
    });

    stopButton.setOnAction(event -> {
      stopPlayingMusic();
      addDebugMessage("Pressed stop playback button for track " + audioFile.getDisplayName());
    });
  }

  private void stopPlayingMusic() {
    getMusicListControl().stopMusic();
  }

  private void initializeBpmIndicator() {
    tappedBpm.addListener((observable, oldValue, newValue) -> tappedBpmString.set(String.valueOf(newValue)));
    bpm.textProperty().bind(tappedBpmString);
  }

  private void initializeTap() {
    tap = new Tap(messageProducer, messageFormatBundle);
  }

  private void initializeTapButton() {
    tapButton.setOnAction(event -> tap());
  }

  private void tap() {
    TapEvent tapEvent = this.tap.tap();
    tappedBpm.setValue(tapEvent.getBpm());
    if (tapEvent.isConverged()) {
      selectedBpm.setValue(tapEvent.getBpm());
      selectedBPMSpinner.getValueFactory().setValue(tapEvent.getBpm());
    }
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void initializeSelectedBpmSpinner() {
    selectedBPMSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      selectedBpm.setValue(newValue);
      messageProducer.send(new DebugMessage(BpmWindow.this, "Changed contents of field 'selectedBpm' from: '" + oldValue + "' to '" + newValue + "'"));
    });
  }

  private void initializeButtons() {
    cancelButton.setOnAction(event -> close());
    okButton.setOnAction(event -> {
      updateBpmWith(index, selectedBpm.get());
      close();
    });
  }

  private void initializeFieldsWithExistingBpmValue() {
    if (audioFile != null && audioFile.hasBpm()) {
      tappedBpm.setValue(audioFile.getBpm());
      selectedBpm.setValue(audioFile.getBpm());
    }
  }

  protected void playTrack(int index) {
    getMusicListControl().playMusicTrack(index);
  }

  protected void updateBpmWith(int index, int bpm) {
    getMusicListControl().setMusicBpm(index, bpm);
  }

  MusicListControl getMusicListControl() {
    return musicListControl;
  }

  private void close() {
    stopPlayingMusic();
    parent.getScene().getWindow().hide();
  }
}
