package ch.retorte.intervalmusiccompositor.ui.soundeffects;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.DebugMessageEventHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Collection;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;

class SoundEffectEntry extends HBox {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/SoundEffectsEntry.fxml";


  //---- Fields

  private MessageFormatBundle bundle = getBundle(UI_RESOURCE_BUNDLE_NAME);

  private SoundEffectsPane parent;
  private SoundEffectOccurrence soundEffectOccurrence;
  private SoundEffectsProvider soundEffectsProvider;
  private MusicListControl musicListControl;
  private MessageProducer messageProducer;


  //---- FX fields

  @FXML
  private ComboBox<SoundEffect> soundEffects;

  @FXML
  private Button playSoundEffect;

  @FXML
  private Spinner<Integer> soundEffectStartTime;

  @FXML
  private Button removeSoundEffect;


  //---- Constructor

  SoundEffectEntry(SoundEffectsPane parent, SoundEffectOccurrence soundEffectOccurrence, SoundEffectsProvider soundEffectsProvider, MusicListControl musicListControl, MessageProducer messageProducer) {
    this.parent = parent;
    this.soundEffectOccurrence = soundEffectOccurrence;
    this.soundEffectsProvider = soundEffectsProvider;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;

    initialize();
    initializeFields();
  }


  //---- Methods

  private void initialize() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LAYOUT_FILE), bundle.getBundle());
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void initializeFields() {
    initializeComboBox();
    initializePlayButton();
    initializeSpinner();
    initializeRemoveButton();
  }

  private void initializeComboBox() {
    Collection<SoundEffect> soundEffects = soundEffectsProvider.getSoundEffects();

    // We provide custom cell factories for label providing.
    this.soundEffects.setCellFactory(param -> new SoundEffectListCell());
    this.soundEffects.setButtonCell(new SoundEffectListCell());

    this.soundEffects.valueProperty().addListener(debugHandlerWith(this.soundEffects.getId()));
    this.soundEffects.setItems(FXCollections.observableArrayList(soundEffects));
    this.soundEffects.setValue(soundEffectOccurrence.getSoundEffect());
    this.soundEffects.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectedSoundEffect());
  }

  private void initializePlayButton() {
    playSoundEffect.onActionProperty().addListener(debugHandlerWith(playSoundEffect.getId()));
    playSoundEffect.setOnAction(event -> {
      if (soundEffectOccurrence.getSoundEffect() != null) {
        musicListControl.playSoundEffect(soundEffectOccurrence.getSoundEffect());
      }
    });
  }

  private void initializeSpinner() {
    soundEffectStartTime.getValueFactory().setValue((int) (soundEffectOccurrence.getTimeMillis() / 1000));
    soundEffectStartTime.valueProperty().addListener(debugHandlerWith(soundEffectStartTime.getId()));
    soundEffectStartTime.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectedSoundEffect());
  }

  private void initializeRemoveButton() {
    removeSoundEffect.setOnAction(event -> parent.removeEntry(this));
  }

  SoundEffectOccurrence getSoundEffectOccurrence() {
    return soundEffectOccurrence;
  }

  void updateSpinnerSizeWith(int max) {
    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) soundEffectStartTime.getValueFactory();
    valueFactory.setMax(max);
  }

  private void updateSelectedSoundEffect() {
    soundEffectOccurrence.setSoundEffect(soundEffects.getValue());
    soundEffectOccurrence.setTimeMillis(soundEffectStartTime.getValueFactory().getValue() * 1000);

    parent.updateEntry();
  }

  private DebugMessageEventHandler debugHandlerWith(String id) {
    return new DebugMessageEventHandler(SoundEffectEntry.this, id, messageProducer);
  }

}
