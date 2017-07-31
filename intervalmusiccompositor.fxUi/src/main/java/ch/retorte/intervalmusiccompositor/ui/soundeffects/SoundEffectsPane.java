package ch.retorte.intervalmusiccompositor.ui.soundeffects;


import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.DebugMessageEventHandler;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


import java.io.IOException;
import java.util.List;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;
import static com.google.common.collect.Lists.newLinkedList;

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

  private List<SoundEffectEntry> soundEffectEntries = newLinkedList();


  //---- FX fields

  @FXML
  private Button addSoundEffects;

  @FXML
  private VBox soundEffectsContainer;


  //---- Constructor

  public SoundEffectsPane(SoundEffectsProvider soundEffectsProvider, CompilationParameters compilationParameters, MusicListControl musicListControl, MessageProducer messageProducer, ChangeListener<SoundEffectOccurrence> soundEffectsUpdateListener) {
    this.soundEffectsProvider = soundEffectsProvider;
    this.compilationParameters = compilationParameters;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;
    this.soundEffectsUpdateListener = soundEffectsUpdateListener;

    initialize();
    initializeAddButton();
  }

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

  //---- Methods

  public ChangeListener<Void> getMusicAndBreakPatternChangeListener() {
    return (observable, oldValue, newValue) -> {
      activateButton(compilationParameters.hasUsableData());
      adaptSpinnerRangeToMusicAndBreakSize();
    };
  }

  private void activateButton(boolean active) {
    addSoundEffects.setDisable(!active);
  }

  private void adaptSpinnerRangeToMusicAndBreakSize() {
    updateSpinnerSizeWith(getSmallestPatternPairFromCompilationParameters() - 1);
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

  private void initializeAddButton() {
    addSoundEffects.onActionProperty().addListener(debugHandlerWith(addSoundEffects.getId()));
    addSoundEffects.setOnAction(event -> addNewEntry());
  }

  private void addNewEntry() {
    SoundEffectOccurrence soundEffectOccurrence = new SoundEffectOccurrence(soundEffectsProvider.getSoundEffects().iterator().next(), 0);
    SoundEffectEntry soundEffectEntry = new SoundEffectEntry(this, soundEffectOccurrence, soundEffectsProvider, musicListControl, messageProducer);

    soundEffectEntries.add(soundEffectEntry);
    soundEffectsContainer.getChildren().add(soundEffectEntry);
    compilationParameters.addSoundEffectOccurrence(soundEffectOccurrence);

    adaptSpinnerRangeToMusicAndBreakSize();

    messageProducer.send(new DebugMessage(this, "Added sound effect '" + soundEffectOccurrence.getSoundEffect().getId() + "' at '" + soundEffectOccurrence.getTimeMillis() + "'."));
    fireEffectChangeListener();
  }

  void updateEntry() {
    fireEffectChangeListener();
  }

  void removeEntry(SoundEffectEntry soundEffectEntry) {
    SoundEffectOccurrence soundEffectOccurrence = soundEffectEntry.getSoundEffectOccurrence();
    compilationParameters.removeSoundEffectOccurrence(soundEffectOccurrence);
    soundEffectEntries.remove(soundEffectEntry);
    soundEffectsContainer.getChildren().remove(soundEffectEntry);

    messageProducer.send(new DebugMessage(this, "Removed sound effect '" + soundEffectOccurrence.getSoundEffect().getId() + "' at '" + soundEffectOccurrence.getTimeMillis() + "'."));
    fireEffectChangeListener();
  }

  private void fireEffectChangeListener() {
    soundEffectsUpdateListener.changed(null, null, null);
  }

  private void updateSpinnerSizeWith(int max) {
    soundEffectEntries.forEach(e -> e.updateSpinnerSizeWith(max));
  }

  private DebugMessageEventHandler debugHandlerWith(String id) {
    return new DebugMessageEventHandler(SoundEffectsPane.this, id, messageProducer);
  }


}
