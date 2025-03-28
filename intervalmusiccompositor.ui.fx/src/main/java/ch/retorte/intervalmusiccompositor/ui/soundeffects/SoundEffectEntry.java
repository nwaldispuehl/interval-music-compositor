package ch.retorte.intervalmusiccompositor.ui.soundeffects;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.ui.bundle.UiBundleProvider;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.DebugMessageEventHandler;
import ch.retorte.intervalmusiccompositor.ui.utils.WidgetTools;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Collection;

class SoundEffectEntry extends HBox {

    //---- Static

    private static final String LAYOUT_FILE = "/layouts/SoundEffectsEntry.fxml";


    //---- Fields

    private final MessageFormatBundle bundle = new UiBundleProvider().getBundle();

    private final SoundEffectsPane parent;
    private final SoundEffectOccurrence soundEffectOccurrence;
    private final SoundEffectsProvider soundEffectsProvider;
    private final MusicListControl musicListControl;
    private final MessageProducer messageProducer;

    private final WidgetTools widgetTools = new WidgetTools();


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
        this.soundEffects.valueProperty().addListener((observable, oldValue, newValue) -> parent.updatePreferences());
        this.soundEffects.valueProperty().addListener((observable, oldValue, newValue) -> parent.adaptSpinnerRangeToMusicAndBreakSize());
    }

    private void initializePlayButton() {
        playSoundEffect.setOnAction(event -> {
            if (soundEffectOccurrence.getSoundEffect() != null) {
                musicListControl.playSoundEffect(soundEffectOccurrence.getSoundEffect());
            }
        });
    }

    private void initializeSpinner() {
        soundEffectStartTime.getValueFactory().setValue((int) (soundEffectOccurrence.getStartTimeMs() / 1000));
        soundEffectStartTime.valueProperty().addListener(debugHandlerWith(soundEffectStartTime.getId()));
        soundEffectStartTime.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectedSoundEffect());
        soundEffectStartTime.valueProperty().addListener((observable, oldValue, newValue) -> parent.updatePreferences());

        widgetTools.prepare(soundEffectStartTime);
    }

    private void initializeRemoveButton() {
        removeSoundEffect.setOnAction(event -> parent.removeEntry(this));
    }

    SoundEffectOccurrence getSoundEffectOccurrence() {
        return soundEffectOccurrence;
    }

    void updateSpinnerSizeWith(int maximalTrackDuration) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) soundEffectStartTime.getValueFactory();
        valueFactory.setMax(getLatestStartTimeWith(maximalTrackDuration));
    }

    private int getLatestStartTimeWith(int maximalTrackDuration) {
        return maximalTrackDuration - (int) Math.ceil(soundEffectOccurrence.getSoundEffect().getDurationMs() / 1000.0);
    }

    private void updateSelectedSoundEffect() {
        soundEffectOccurrence.setSoundEffect(soundEffects.getValue());
        soundEffectOccurrence.setStartTimeMs(soundEffectStartTime.getValueFactory().getValue() * 1000);

        parent.updateEntry();
    }

    private DebugMessageEventHandler debugHandlerWith(String id) {
        return new DebugMessageEventHandler(SoundEffectEntry.this, id, messageProducer);
    }

}
