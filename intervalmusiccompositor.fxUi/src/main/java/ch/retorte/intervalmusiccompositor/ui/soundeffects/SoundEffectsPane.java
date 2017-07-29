package ch.retorte.intervalmusiccompositor.ui.soundeffects;


import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.DebugMessageEventHandler;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;


import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;

public class SoundEffectsPane extends BorderPane {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/SoundEffectsPane.fxml";


  //---- Fields

  private MessageFormatBundle bundle = getBundle(UI_RESOURCE_BUNDLE_NAME);
  private SoundEffectsProvider soundEffectsProvider;
  private CompilationParameters compilationParameters;
  private MusicListControl musicListControl;
  private MessageProducer messageProducer;
  private ChangeListener<SoundEffectOccurrence> soundEffectsUpdateListener;


  //---- FX fields

  @FXML
  private CheckBox addSoundEffects;

  @FXML
  private HBox soundEffectsContainer;

  @FXML
  private ComboBox<SoundEffect> soundEffects;

  @FXML
  private Button playSoundEffect;

  @FXML
  private Spinner<Integer> soundEffectStartTime;

  //---- Constructor

  public SoundEffectsPane(SoundEffectsProvider soundEffectsProvider, CompilationParameters compilationParameters, MusicListControl musicListControl, MessageProducer messageProducer, ChangeListener<SoundEffectOccurrence> soundEffectsUpdateListener) {
    this.soundEffectsProvider = soundEffectsProvider;
    this.compilationParameters = compilationParameters;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;
    this.soundEffectsUpdateListener = soundEffectsUpdateListener;

    initialize();
    initializeFields();
  }

  private void initialize() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LAYOUT_FILE), bundle.getBundle());
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception);
    }
  }

  //---- Methods

  public ChangeListener<Void> getMusicAndBreakPatternChangeListener() {
    return (observable, oldValue, newValue) -> {
      activateCheckbox(compilationParameters.hasUsableData());
      updateSpinnerSizeWith(getSmallestPatternPairFromCompilationParameters() - 1);
    };
  }

  private void activateCheckbox(boolean active) {
    addSoundEffects.setDisable(!active);
  }

  private int getSmallestPatternPairFromCompilationParameters() {
    List<Integer> musicPattern = compilationParameters.getMusicPattern();
    List<Integer> breakPattern = compilationParameters.getBreakPattern();

    int smallestPatternPair = Integer.MAX_VALUE;
    for (int i = 0; i < musicPattern.size(); i++) {
      int m = musicPattern.get(i);
      int b = 0;
      if (!breakPattern.isEmpty()) {
        b = breakPattern.get(i % breakPattern.size());
      }

      if (m + b < smallestPatternPair) {
        smallestPatternPair = m + b;
      }
    }
    return smallestPatternPair;
  }

  private void initializeFields() {
    initializeContainer();
    initializeCheckBox();
    initializeComboBox();
    initializePlayButton();
    initializeSpinner();
  }

  private void initializeContainer() {
    soundEffectsContainer.managedProperty().bind(soundEffectsContainer.visibleProperty());
  }

  private void initializeCheckBox() {
    addSoundEffects.selectedProperty().addListener(debugHandlerWith(addSoundEffects.getId()));
    addSoundEffects.selectedProperty().addListener((observable, previouslyChecked, checked) -> {
      soundEffectsContainer.setVisible(checked);
      if (!checked) {
        compilationParameters.clearSoundEffects();
      }
      else {
        updateSelectedSoundEffect();
      }

      soundEffectsUpdateListener.changed(null, null, null);
    });
  }

  private void initializeComboBox() {
    Collection<SoundEffect> soundEffects = soundEffectsProvider.getSoundEffects();

    // We provide custom cell factories for label providing.
    this.soundEffects.setCellFactory(param -> new SoundEffectListCell());
    this.soundEffects.setButtonCell(new SoundEffectListCell());

    this.soundEffects.valueProperty().addListener(debugHandlerWith(this.soundEffects.getId()));
    this.soundEffects.setItems(FXCollections.observableArrayList(soundEffects));
    this.soundEffects.setValue(soundEffects.iterator().next());
    this.soundEffects.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectedSoundEffect());
  }

  private void initializePlayButton() {
    playSoundEffect.onActionProperty().addListener(debugHandlerWith(playSoundEffect.getId()));
    playSoundEffect.setOnAction(event -> {
        if (soundEffects.getValue() != null) {
          musicListControl.playSoundEffect(soundEffects.getValue());
        }
    });
  }

  private void initializeSpinner() {
    soundEffectStartTime.valueProperty().addListener(debugHandlerWith(soundEffectStartTime.getId()));
    soundEffectStartTime.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectedSoundEffect());
  }

  private void updateSpinnerSizeWith(int max) {
//    soundEffectStartTime.getValueFactory().
  }

  private void updateSelectedSoundEffect() {
    SoundEffect soundEffect = soundEffects.getValue();
    long startTimeMillis = (long) (soundEffectStartTime.getValue() * 1000);

    SoundEffectOccurrence soundEffectOccurrence = new SoundEffectOccurrence(soundEffect, startTimeMillis);
    compilationParameters.setSoundEffectOccurrence(soundEffectOccurrence);
    soundEffectsUpdateListener.changed(null, null, soundEffectOccurrence);
  }

  private DebugMessageEventHandler debugHandlerWith(String id) {
    return new DebugMessageEventHandler(SoundEffectsPane.this, id, messageProducer);
  }


}
