package ch.retorte.intervalmusiccompositor.ui.mainscreen;

import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.model.list.BlendMode;
import ch.retorte.intervalmusiccompositor.model.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.model.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ProgressMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.SubProcessProgressMessage;
import ch.retorte.intervalmusiccompositor.spi.*;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.spi.update.VersionUpgrader;
import ch.retorte.intervalmusiccompositor.ui.audiofilelist.DraggableAudioFileBreakListView;
import ch.retorte.intervalmusiccompositor.ui.audiofilelist.DraggableAudioFileListView;
import ch.retorte.intervalmusiccompositor.ui.audiofilelist.MusicFileChooser;
import ch.retorte.intervalmusiccompositor.ui.bundle.UiBundleProvider;
import ch.retorte.intervalmusiccompositor.ui.debuglog.DebugLogWindow;
import ch.retorte.intervalmusiccompositor.ui.graphics.BarChart;
import ch.retorte.intervalmusiccompositor.ui.preferences.PreferencesWindow;
import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
import ch.retorte.intervalmusiccompositor.ui.soundeffects.SoundEffectsPane;
import ch.retorte.intervalmusiccompositor.ui.updatecheck.UpdateCheckDialog;
import ch.retorte.intervalmusiccompositor.ui.utils.AudioFileEncoderConverter;
import ch.retorte.intervalmusiccompositor.ui.utils.WidgetTools;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

/**
 * Controller for the main screen of the JavaFX ui.
 */
public class MainScreenController implements Initializable {

    //---- FX Widgets

    @FXML
    private VBox root;

    // Menu

    @FXML
    private MenuItem menuLoadMusicFile;

    @FXML
    private MenuItem menuLoadBreakFile;

    @FXML
    private MenuItem menuPreferences;

    @FXML
    private MenuItem menuQuit;

    @FXML
    private MenuItem menuVersion;

    @FXML
    private MenuItem menuHelp;

    @FXML
    private MenuItem menuAbout;

    @FXML
    private MenuItem menuCheckForUpdates;

    @FXML
    private MenuItem menuShowDebugLog;

    // Content

    @FXML
    private SplitPane container;

    // Music track

    @FXML
    private Label trackCount;

    @FXML
    private Button addMusicTrackButton;

    @FXML
    private DraggableAudioFileListView musicTrackListView;

    @FXML
    private Button sortTrackList;

    @FXML
    private Button shuffleTrackList;

    @FXML
    private Label trackListSortOrderIndicator;

    @FXML
    private ToggleGroup enumerationToggleGroup;

    @FXML
    private RadioButton singleExtractEnumeration;

    @FXML
    private RadioButton continuousEnumeration;

    // Break track

    @FXML
    private Button addBreakTrackButton;

    @FXML
    private Label breakVolumeLabel;

    @FXML
    private Slider breakVolume;

    @FXML
    private DraggableAudioFileBreakListView breakTrackListView;

    // Visual

    @FXML
    private ImageView imageView;

    @FXML
    private Image starterBackgroundImage;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressBar secondaryProgressBar;

    // Track controls

    @FXML
    private TabPane periodTabPane;

    @FXML
    private Tab simpleTab;

    @FXML
    private Spinner<Integer> soundPeriod;

    @FXML
    private Spinner<Integer> breakPeriod;

    @FXML
    private TextField soundPattern;

    @FXML
    private TextField breakPattern;

    @FXML
    private Spinner<Integer> iterations;

    @FXML
    private Label duration;

    @FXML
    private ToggleGroup blendModeToggleGroup;

    @FXML
    private RadioButton separateBlendMode;

    @FXML
    private RadioButton crossBlendMode;

    @FXML
    private Slider blendDuration;


    // Sound effects

    @FXML
    private Pane soundEffectsContainer;

    // File format

    @FXML
    private ChoiceBox<AudioFileEncoder> outputFileFormat;

    // Output directory

    @FXML
    private Button chooseOutputDirectory;

    @FXML
    private Button clearOutputDirectory;

    @FXML
    private Label outputDirectory;

    // Actions

    @FXML
    private Button process;


    // Bindings

    private final BooleanProperty hasUsableData = new SimpleBooleanProperty(false);
    private final BooleanProperty hasUsableTracks = new SimpleBooleanProperty(false);


    // Fields

    private SoundEffectsPane soundEffectsPane;

    private final MessageFormatBundle coreBundle = new CoreBundleProvider().getBundle();
    private final MessageFormatBundle bundle = new UiBundleProvider().getBundle();

    private Ui ui;
    private ProgramControl programControl;
    private ApplicationData applicationData;
    private VersionUpgrader versionUpgrader;
    private MusicListControl musicListControl;
    private MusicCompilationControl musicCompilationControl;
    private CompilationParameters compilationParameters;
    private MessageProducer messageProducer;
    private UpdateAvailabilityChecker updateAvailabilityChecker;
    private ScheduledExecutorService executorService;
    private SoundEffectsProvider soundEffectsProvider;
    private UiUserPreferences userPreferences;
    private ObservableList<AudioFileDecoder> audioFileDecoders;
    private ObservableList<AudioFileEncoder> audioFileEncoders;

    private ChangeListener<Void> musicAndBreakPatternChangeListener;

    private final WidgetTools widgetTools = new WidgetTools();

    //---- Methods

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        // Method is needed to satisfy interface definition. We don't need to initialize anything here, though.
    }

    public void initializeFieldsWith(Ui ui, ProgramControl programControl, ApplicationData applicationData, VersionUpgrader versionUpgrader, MusicListControl musicListControl, MusicCompilationControl musicCompilationControl, CompilationParameters compilationParameters, MessageSubscriber messageSubscriber, MessageProducer messageProducer, UpdateAvailabilityChecker updateAvailabilityChecker, ScheduledExecutorService executorService, SoundEffectsProvider soundEffectsProvider, List<AudioFileDecoder> audioFileDecoders, List<AudioFileEncoder> audioFileEncoders, UiUserPreferences userPreferences) {
        this.ui = ui;
        this.programControl = programControl;
        this.applicationData = applicationData;
        this.versionUpgrader = versionUpgrader;
        this.musicListControl = musicListControl;
        this.musicCompilationControl = musicCompilationControl;
        this.compilationParameters = compilationParameters;
        this.messageProducer = messageProducer;
        this.updateAvailabilityChecker = updateAvailabilityChecker;
        this.executorService = executorService;
        this.soundEffectsProvider = soundEffectsProvider;
        this.audioFileDecoders = FXCollections.observableArrayList(audioFileDecoders);
        this.audioFileEncoders = FXCollections.observableArrayList(audioFileEncoders);
        this.userPreferences = userPreferences;

        initializeStyles();

        initializeMenu();
        initializeMusicTrackList();
        initializeTrackEnumeration();
        initializeBreakTrackList();
        initializeDurationControls();
        initializeBlending();
        initializeSoundEffects();
        initializeOutputFileFormat();
        initializeOutputDirectory();
        initializeControlButtons();
        initializeBackgroundImage();
        initializeControlButtonsActivation();

        initializePreferenceStorage();

        addMessageSubscribers(messageSubscriber);
    }

    private void initializeStyles() {
        root.getStylesheets().addAll("/styles/fonts.css", "/styles/MainScreen.css");
    }

    private void initializeMenu() {
        menuLoadMusicFile.setOnAction(event -> openFileChooserFor(musicTrackListView));
        menuLoadBreakFile.setOnAction(event -> openFileChooserFor(breakTrackListView));
        menuPreferences.setOnAction(event -> openPreferencesWindow());
        menuQuit.setOnAction(event -> ui.quit());

        menuVersion.setText(getVersionText());
        menuHelp.setOnAction(event -> openHelpWebsite());
        menuAbout.setOnAction(event -> openAboutWebsite());
        menuCheckForUpdates.setOnAction(event -> openUpdateCheckDialog());
        menuShowDebugLog.setOnAction(event -> openDebugLog());
    }

    private String getVersionText() {
        return "Version " + applicationData.getProgramVersion();
    }

    private void openHelpWebsite() {
        ui.openInDesktopBrowser(getHelpWebsiteUrl());
    }

    private String getHelpWebsiteUrl() {
        return coreBundle.getString("web.website.help.url");
    }

    private void openAboutWebsite() {
        ui.openInDesktopBrowser(getAboutWebsiteUrl());
    }

    private void openUpdateCheckDialog() {
        new UpdateCheckDialog(updateAvailabilityChecker, ui, bundle, coreBundle, userPreferences, applicationData, versionUpgrader, messageProducer).startVersionCheck();
    }

    private String getAboutWebsiteUrl() {
        return coreBundle.getString("web.website.about.url");
    }

    private void openDebugLog() {
        DebugLogWindow debugLogWindow = new DebugLogWindow(bundle, programControl, executorService);
        debugLogWindow.show();
    }

    private void initializeMusicTrackList() {
        musicTrackListView.initializeWith(musicListControl.getMusicList(), bundle, messageProducer, musicListControl, e -> Platform.runLater(this::updateUiDataWidgets));
        musicTrackListView.addListChangeListener(newValue -> updateUiDataWidgets());
        addMusicTrackButton.setOnAction(event -> openFileChooserFor(musicTrackListView));
        addMusicTrackButton.graphicProperty().set(imageFrom("/images/add_icon_small.png"));
    }

    private ImageView imageFrom(String resourcePath) {
        return new ImageView(new Image(MainScreenController.class.getResourceAsStream(resourcePath)));
    }

    private void initializeTrackEnumeration() {
        sortTrackList.setOnAction(event -> {
            addDebugMessage("Pressed 'sort' button.");
            musicListControl.sortMusicList();
            updateUiDataWidgets();
        });

        shuffleTrackList.setOnAction(event -> {
            addDebugMessage("Pressed 'shuffle' button.");
            musicListControl.shuffleMusicList();
            updateUiDataWidgets();
        });

        trackListSortOrderIndicator.textProperty().addListener(debugHandlerWith(trackListSortOrderIndicator.getId()));

        enumerationToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setEnumerationMode((String) newValue.getUserData()));
        enumerationToggleGroup.selectedToggleProperty().addListener(debugHandlerWith("enumerationToggleGroup"));
        enumerationToggleGroup.selectedToggleProperty().addListener(updateUiHandler());

        singleExtractEnumeration.graphicProperty().set(imageFrom("/images/enumeration_mode_1.png"));
        continuousEnumeration.graphicProperty().set(imageFrom("/images/enumeration_mode_2.png"));
    }

    private void initializeBreakTrackList() {
        breakTrackListView.initializeWith(musicListControl.getBreakList(), bundle, messageProducer, musicListControl, e -> Platform.runLater(this::updateUiDataWidgets));
        addBreakTrackButton.setOnAction(event -> openFileChooserFor(breakTrackListView));
        addBreakTrackButton.graphicProperty().set(imageFrom("/images/add_icon_small.png"));

        breakVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volume = newValue.doubleValue();
            if (((int) volume) == volume) {
                /* The slider fires not only for the configured integer positions, but also in between (e.g. for 1.023323). We skip such data. */
                breakVolumeLabel.setText(bundle.getString("ui.form.break_list.volume", volume));
                compilationParameters.setBreakVolume(volume / 100.0);
            }
        });
        breakVolume.valueProperty().addListener(debugHandlerWith(breakVolume.getId()));
        breakVolumeLabel.setText(bundle.getString("ui.form.break_list.volume", breakVolume.valueProperty().intValue()));
    }

    private void initializeDurationControls() {
        periodTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updatePeriodSelectionWith(newValue));

        soundPeriod.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setMusicPattern(newValue));
        breakPeriod.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setBreakPattern(newValue));
        soundPattern.textProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setMusicPattern(newValue));
        breakPattern.textProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setBreakPattern(newValue));
        iterations.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setIterations(newValue));

        widgetTools.prepare(soundPeriod);
        widgetTools.prepare(breakPeriod);
        widgetTools.prepare(iterations);

        periodTabPane.getSelectionModel().selectedItemProperty().addListener(updateUiHandler());

        soundPeriod.valueProperty().addListener(debugHandlerWith(soundPeriod.getId()));
        soundPeriod.valueProperty().addListener(updateUiHandler());

        breakPeriod.valueProperty().addListener(debugHandlerWith(breakPeriod.getId()));
        breakPeriod.valueProperty().addListener(updateUiHandler());

        soundPattern.textProperty().addListener(debugHandlerWith(soundPattern.getId()));
        soundPattern.textProperty().addListener(updateUiHandler());

        breakPattern.textProperty().addListener(debugHandlerWith(breakPattern.getId()));
        breakPattern.textProperty().addListener(updateUiHandler());

        iterations.valueProperty().addListener(debugHandlerWith(iterations.getId()));
        iterations.valueProperty().addListener(updateUiHandler());
    }

    private void initializeBlending() {
        blendModeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setBlendMode((String) newValue.getUserData()));
        blendModeToggleGroup.selectedToggleProperty().addListener(debugHandlerWith("blendModeToggleGroup"));
        blendModeToggleGroup.selectedToggleProperty().addListener(updateUiHandler());

        blendDuration.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setBlendDuration(newValue.doubleValue()));
        blendDuration.valueProperty().addListener(debugHandlerWith(blendDuration.getId()));
        blendDuration.valueProperty().addListener(updateUiHandler());

        separateBlendMode.graphicProperty().set(imageFrom("/images/blend_mode_1.png"));
        crossBlendMode.graphicProperty().set(imageFrom("/images/blend_mode_2.png"));
    }

    private void initializeSoundEffects() {
        soundEffectsPane = new SoundEffectsPane(soundEffectsProvider, compilationParameters, musicListControl, messageProducer, (obs, oldVal, newVal) -> updateEnvelopeImage(), userPreferences);
        musicAndBreakPatternChangeListener = soundEffectsPane.getMusicAndBreakPatternChangeListener();
        soundEffectsContainer.getChildren().add(soundEffectsPane);
    }

    private void initializeOutputFileFormat() {
        outputFileFormat.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setEncoderIdentifier(newValue.getIdentificator()));
        outputFileFormat.valueProperty().addListener(debugHandlerWith(outputFileFormat.getId()));

        outputFileFormat.setConverter(new AudioFileEncoderConverter(audioFileEncoders));
        outputFileFormat.setItems(audioFileEncoders);
        outputFileFormat.getSelectionModel().selectFirst();
    }

    private void initializeOutputDirectory() {
        outputDirectory.textProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setOutputPath(newValue));
        outputDirectory.textProperty().addListener(debugHandlerWith(outputDirectory.getId()));
        chooseOutputDirectory.setOnAction(event -> openDirectoryChooser());
        clearOutputDirectory.setOnAction(event -> resetOutputDirectory());
        clearOutputDirectory.graphicProperty().set(imageFrom("/images/clear_icon_small.png"));
    }

    private void resetOutputDirectory() {
        outputDirectory.textProperty().setValue(bundle.getString("ui.form.outfile_label"));
        compilationParameters.resetOutputPath();
        userPreferences.saveOutputDirectory(null);
    }

    private void initializeControlButtons() {
        process.disableProperty().bind(hasUsableData.not().or(hasUsableTracks.not()));
        process.setOnAction(event -> musicCompilationControl.startCompilation(compilationParameters));
    }

    private void initializeBackgroundImage() {
        InputStream starterBackgroundInputStream = MainScreenController.class.getResourceAsStream("/images/starter_background.png");
        if (starterBackgroundInputStream != null) {
            starterBackgroundImage = new Image(starterBackgroundInputStream);
            imageView.setImage(starterBackgroundImage);
        }
    }

    private void initializeControlButtonsActivation() {
        UsableDataChangeListener l = new UsableDataChangeListener();
        soundPeriod.valueProperty().addListener(l);
        breakPeriod.valueProperty().addListener(l);
        soundPattern.textProperty().addListener(l);
        breakPattern.textProperty().addListener(l);
        iterations.valueProperty().addListener(l);

        updateUsableData();
        markUsableTracks();
    }

    private void initializePreferenceStorage() {

        try {
            if (settingsShouldBeLoaded()) {
                addDebugMessage("Loading stored compilation parameters from the last session.");
                loadStoredPreferenceValues();
                /* Sound effect start times can not be larger than the length of music and break times. We thus need to first load those values. */
                soundEffectsPane.loadStoredPreferenceValues();
            } else {
                addDebugMessage("Skipping loading stored compilation parameters from the last session.");
            }
        } catch (Exception e) {
            // An error when loading preferences can easily happen due to the change of field identifiers etc. We just ignore it.
            addDebugMessage("Failed to load preference values: " + e.getLocalizedMessage());
        }

        registerPreferenceSaveListeners();
    }

    private boolean settingsShouldBeLoaded() {
        return userPreferences.loadSaveFieldState();
    }

    private void loadStoredPreferenceValues() {
        List<File> musicTrackInputList = userPreferences.loadMusicTrackList();
        addDebugMessage("Retrieved music track files: " + musicTrackInputList);
        for (File f : musicTrackInputList) {
            musicTrackListView.addTrack(f);
        }

        List<File> breakTrackInputList = userPreferences.loadBreakTrackList();
        addDebugMessage("Retrieved break track files: " + breakTrackInputList);
        for (File f : breakTrackInputList) {
            breakTrackListView.addTrack(f);
        }

        enumerationToggleGroup.selectToggle(getEnumerationToggleFor(userPreferences.loadEnumerationMode(EnumerationMode.SINGLE_EXTRACT)));

        soundPeriod.getValueFactory().setValue(userPreferences.loadSoundPeriod(0));
        breakPeriod.getValueFactory().setValue(userPreferences.loadBreakPeriod(0));
        soundPattern.textProperty().setValue(userPreferences.loadSoundPattern(""));
        breakPattern.textProperty().setValue(userPreferences.loadBreakPattern(""));

        breakVolume.valueProperty().setValue(userPreferences.loadBreakVolume(100));

        Tab selectedTab = getPeriodTabFor(userPreferences.loadPeriodTab("simpleTab"));
        periodTabPane.getSelectionModel().select(selectedTab);
        updatePeriodSelectionWith(selectedTab);

        blendModeToggleGroup.selectToggle(getBlendModeToggleFor(userPreferences.loadBlendMode(BlendMode.SEPARATE)));
        iterations.getValueFactory().setValue(userPreferences.loadIterations(0));
        blendDuration.valueProperty().setValue(userPreferences.loadBlendDuration(1));
        outputFileFormat.valueProperty().setValue(getOutputFileFormatFor(userPreferences.loadOutputFileFormat("mp3")));
        if (userPreferences.hasOutputDirectory()) {
            /* Since we misuse the label as information store and display for the default value ('<Desktop>') we should not change it if there is no data. */
            outputDirectory.textProperty().setValue(userPreferences.loadOutputDirectory(null));
        }
    }

    private void registerPreferenceSaveListeners() {
        musicListControl.getMusicList().addListener(n -> userPreferences.saveMusicTrackList(musicListControl.getMusicList()));
        musicListControl.getBreakList().addListener(n -> userPreferences.saveBreakTrackList(musicListControl.getBreakList()));

        enumerationToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveEnumerationMode(EnumerationMode.valueOf((String) newValue.getUserData())));

        soundPeriod.valueProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSoundPeriod(newValue));
        breakPeriod.valueProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveBreakPeriod(newValue));
        soundPattern.textProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSoundPattern(newValue));
        breakPattern.textProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveBreakPattern(newValue));

        breakVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volume = newValue.doubleValue();
            if (((int) volume) == volume) {
                /* The slider fires not only for the configured integer positions, but also in between (e.g. for 1.023323). We skip such data. */
                userPreferences.saveBreakVolume((int) volume);
            }
        });

        periodTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> userPreferences.savePeriodTab(newValue.getId()));

        iterations.valueProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveIterations(newValue));

        blendModeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveBlendMode(BlendMode.valueOf((String) newValue.getUserData())));

        blendDuration.valueProperty().addListener((observable, oldValue, newValue) -> {
            double duration = newValue.doubleValue();
            if ((int) duration == duration) {
                /* The slider fires not only for the configured integer positions, but also in between (e.g. for 1.023323). We skip such data. */
                userPreferences.saveBlendDuration((int) duration);
            }
        });

        outputFileFormat.valueProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveOutputFileFormat(newValue.getIdentificator()));
        outputDirectory.textProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveOutputDirectory(newValue));
    }

    private Tab getPeriodTabFor(String tabId) {
        for (Tab t : periodTabPane.getTabs()) {
            if (t.getId().equals(tabId)) {
                return t;
            }
        }
        return null;
    }

    private Toggle getEnumerationToggleFor(EnumerationMode enumerationMode) {
        for (Toggle t : enumerationToggleGroup.getToggles()) {
            if (t.getUserData().equals(enumerationMode.name())) {
                return t;
            }
        }
        return null;
    }

    private Toggle getBlendModeToggleFor(BlendMode blendMode) {
        for (Toggle t : blendModeToggleGroup.getToggles()) {
            if (t.getUserData().equals(blendMode.name())) {
                return t;
            }
        }
        return null;
    }

    private AudioFileEncoder getOutputFileFormatFor(String outputFileFormatIdentifier) {
        for (AudioFileEncoder e : audioFileEncoders) {
            if (e.getIdentificator().equals(outputFileFormatIdentifier)) {
                return e;
            }
        }
        return null;
    }

    private void openDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (!new File(compilationParameters.getOutputPath()).exists()) {
            resetOutputDirectory();
        }

        directoryChooser.setInitialDirectory(new File(compilationParameters.getOutputPath()));
        directoryChooser.setTitle(bundle.getString("ui.form.outfile_dialog_title"));
        File file = directoryChooser.showDialog(getWindow());
        if (file != null && file.exists()) {
            outputDirectory.setText(file.getAbsolutePath());
        }
    }

    private void updatePeriodSelectionWith(Tab tab) {
        if (isSimpleTab(tab)) {
            compilationParameters.setMusicPattern(soundPeriod.getValue());
            compilationParameters.setBreakPattern(breakPeriod.getValue());
        } else {
            compilationParameters.setMusicPattern(soundPattern.getText());
            compilationParameters.setBreakPattern(breakPattern.getText());
        }
    }

    private boolean isSimpleTab(Tab tab) {
        return tab.equals(simpleTab);
    }



    private int getDurationEstimation() {
        return compilationParameters.getDurationEstimationSeconds();
    }

    private void setDurationTo(int durationSeconds) {
        duration.setText(new FormatTime().getStrictFormattedTime(durationSeconds));
    }

    public void setActive() {
        container.setDisable(false);
        setMenusDisabled(false);
    }

    public void setInactive() {
        container.setDisable(true);
        setMenusDisabled(true);
    }

    private void setMenusDisabled(boolean disabled) {
        menuLoadMusicFile.setDisable(disabled);
        menuLoadBreakFile.setDisable(disabled);
    }

    private String getLabelFor(ListSortMode listSortMode) {
        return switch (listSortMode) {
            case MANUAL -> bundle.getString("ui.form.music_list.list_mode.manual");
            case SHUFFLE -> bundle.getString("ui.form.music_list.list_mode.shuffle");
            case SORT -> bundle.getString("ui.form.music_list.list_mode.sort");
            case SORT_REV -> bundle.getString("ui.form.music_list.list_mode.sort_rev");
        };
    }

    public void setEnvelopeImage(BufferedImage envelopeImage) {
        imageView.setImage(SwingFXUtils.toFXImage(envelopeImage, null));
    }

    private void openFileChooserFor(DraggableAudioFileListView listView) {
        MusicFileChooser musicFileChooser = new MusicFileChooser(messageProducer, bundle, audioFileDecoders);
        List<File> chosenFile = musicFileChooser.chooseFileIn(getWindow());
        chosenFile.forEach(listView::addTrack);
    }

    private Window getWindow() {
        return container.getScene().getWindow();
    }

    private void openPreferencesWindow() {
        new PreferencesWindow(bundle, userPreferences, applicationData).show();
    }

    private void addMessageSubscribers(MessageSubscriber messageSubscriber) {

        messageSubscriber.addHandler(new MessageHandler<ProgressMessage>() {
            @Override
            public void handle(ProgressMessage message) {
                updateProgressBar(message.getProgressInPercent(), message.getCurrentActivity());
            }
        });

        messageSubscriber.addHandler(new MessageHandler<ErrorMessage>() {
            @Override
            public void handle(ErrorMessage message) {
                showErrorMessage(message.getMessage());
                setActive();
            }
        });

        messageSubscriber.addHandler(new MessageHandler<SubProcessProgressMessage>() {
            @Override
            public void handle(SubProcessProgressMessage message) {
                updateSubProgressBar(message.getPercentage());
            }
        });
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            ButtonType sendErrorMailButtonType = new ButtonType(bundle.getString("ui.error.sendback.button"), ButtonBar.ButtonData.LEFT);
            Alert alert = new Alert(Alert.AlertType.ERROR, "", sendErrorMailButtonType, ButtonType.OK);
            alert.setResizable(true);
            alert.setTitle(bundle.getString("ui.error.title"));
            alert.setContentText(message + "\n\n" + bundle.getString("ui.error.appendix"));
            alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));

            Button sendErrorMailButton = (Button) alert.getDialogPane().lookupButton(sendErrorMailButtonType);
            sendErrorMailButton.setOnAction(event -> {
                openErrorFeedbackMail();
                openDebugLog();
                event.consume();
            });

            alert.show();
        });
    }

    private void openErrorFeedbackMail() {
        ui.openInMailProgram(getErrorFeedbackMailUrl());
    }

    private String getErrorFeedbackMailUrl() {
        String recipient = coreBundle.getString("mail.support.address");
        String subject = coreBundle.getString("error.feedback.mail.subject");
        String messagePrefix = coreBundle.getString("error.feedback.mail.body.prefix");

        String rawMail = coreBundle.getString("error.feedback.mail.template", recipient, encode(subject), encode(messagePrefix));
        return Pattern.compile("\\+").matcher(rawMail).replaceAll("%20");
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private void updateProgressBar(Integer progressInPercent, String currentActivity) {
        progressBar.progressProperty().setValue((double) progressInPercent / 100);
        progressBar.setTooltip(new Tooltip(currentActivity));
        secondaryProgressBar.setVisible(false);
    }

    private void updateSubProgressBar(int percentage) {
        secondaryProgressBar.setVisible(true);
        secondaryProgressBar.progressProperty().setValue((double) percentage / 100);
    }

    private DebugMessageEventHandler debugHandlerWith(String id) {
        return new DebugMessageEventHandler(MainScreenController.this, id, messageProducer);
    }

    private UpdateDependentUiWidgetsHandler updateUiHandler() {
        return new UpdateDependentUiWidgetsHandler();
    }

    private void updateUiDataWidgets() {
        compilationParameters.setListSortMode(musicListControl.getSortMode());
        updateDurationEstimation();
        updateEnvelopeImage();
        updateSortModeLabel();
        updateTrackCount();
        updateSoundEffectPane();
        updateUsableData();
        markUsableTracks();
    }

    private void updateDurationEstimation() {
        setDurationTo(getDurationEstimation());
    }

    private void updateEnvelopeImage() {
        if (compilationParameters.hasUsableData()) {
            BarChart bar = new BarChart(720, 162);
            bar.generate(compilationParameters.getMusicPattern(), compilationParameters.getBreakPattern(), compilationParameters.getIterations(), compilationParameters.getSoundEffectOccurrences(), false);
            imageView.setImage(bar.getWritableImage());
        } else {
            imageView.setImage(starterBackgroundImage);
        }
    }

    private void updateSortModeLabel() {
        trackListSortOrderIndicator.setText(getLabelFor(compilationParameters.getListSortMode()));
    }

    private void updateTrackCount() {
        long tracks = musicListControl.getOkTracks();
        String labelText = bundle.getString("ui.form.music_list.tracks_label_pl", tracks);
        if (tracks == 1) {
            labelText = bundle.getString("ui.form.music_list.tracks_label_sg", tracks);
        }
        trackCount.setText(labelText);
    }

    private void updateSoundEffectPane() {
        musicAndBreakPatternChangeListener.changed(null, null, null);
    }

    private void updateUsableData() {
        hasUsableData.setValue(compilationParameters.hasUsableData());
        hasUsableTracks.setValue(musicListControl.hasUsableTracksWith(compilationParameters));
    }

    private void markUsableTracks() {
        musicListControl.markUsableTracksWith(compilationParameters);
        musicTrackListView.refresh();
        breakTrackListView.refresh();
    }

    private void addDebugMessage(String message) {
        messageProducer.send(new DebugMessage(MainScreenController.this, message));
    }

    //---- Inner classes

    private class UpdateDependentUiWidgetsHandler implements ChangeListener<Object> {
        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            updateUiDataWidgets();
        }
    }

    private class UsableDataChangeListener implements ChangeListener<Object> {
        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            updateUsableData();
        }
    }


}
