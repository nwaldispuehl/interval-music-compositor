package ch.retorte.intervalmusiccompositor.ui.soundeffects;


import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.DebugMessageEventHandler;

import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
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
  private UiUserPreferences userPreferences;

  private List<SoundEffectEntry> soundEffectEntries = newLinkedList();


  //---- FX fields

  @FXML
  private Button addSoundEffects;

  @FXML
  private VBox soundEffectsContainer;


  //---- Constructor

  public SoundEffectsPane(SoundEffectsProvider soundEffectsProvider, CompilationParameters compilationParameters, MusicListControl musicListControl, MessageProducer messageProducer, ChangeListener<SoundEffectOccurrence> soundEffectsUpdateListener, UiUserPreferences userPreferences) {
    this.soundEffectsProvider = soundEffectsProvider;
    this.compilationParameters = compilationParameters;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;
    this.soundEffectsUpdateListener = soundEffectsUpdateListener;
    this.userPreferences = userPreferences;

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

  void adaptSpinnerRangeToMusicAndBreakSize() {
    updateSpinnerSizeWith(getMaximalTrackDuration());
  }

  private int getMaximalTrackDuration() {
    return compilationParameters.getShortestDurationPairFromPattern();
  }

  private void initializeAddButton() {
    addSoundEffects.onActionProperty().addListener(debugHandlerWith(addSoundEffects.getId()));
    addSoundEffects.setOnAction(event -> addNewEntry());
  }

  public void loadStoredPreferenceValues() {
    for (SoundEffectOccurrence s : userPreferences.loadSoundEffectOccurrencesWith(soundEffectsProvider)) {
      addNewEntryWith(s);
    }
  }

  void updatePreferences() {
    userPreferences.saveSoundEffectOccurrences(compilationParameters.getSoundEffectOccurrences());
  }

  private void addNewEntry() {
    SoundEffectOccurrence soundEffectOccurrence = new SoundEffectOccurrence(soundEffectsProvider.getSoundEffects().iterator().next(), 0);
    addNewEntryWith(soundEffectOccurrence);
    updatePreferences();
  }

  private void addNewEntryWith(SoundEffectOccurrence soundEffectOccurrence) {
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
    updatePreferences();
  }

  private void fireEffectChangeListener() {
    soundEffectsUpdateListener.changed(null, null, null);
  }

  private void updateSpinnerSizeWith(int maximalTrackDuration) {
    soundEffectEntries.forEach(e -> e.updateSpinnerSizeWith(maximalTrackDuration));
  }

  private DebugMessageEventHandler debugHandlerWith(String id) {
    return new DebugMessageEventHandler(SoundEffectsPane.this, id, messageProducer);
  }


}
