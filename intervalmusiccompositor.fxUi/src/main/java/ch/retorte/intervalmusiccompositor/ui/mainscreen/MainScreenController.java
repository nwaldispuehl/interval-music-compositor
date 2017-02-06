package ch.retorte.intervalmusiccompositor.ui.mainscreen;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ProgressMessage;
import ch.retorte.intervalmusiccompositor.messagebus.SubProcessProgressMessage;
import ch.retorte.intervalmusiccompositor.spi.*;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.ui.audiofilelist.DraggableAudioFileBreakListView;
import ch.retorte.intervalmusiccompositor.ui.audiofilelist.DraggableAudioFileListView;
import ch.retorte.intervalmusiccompositor.ui.audiofilelist.MusicFileChooser;
import ch.retorte.intervalmusiccompositor.ui.debuglog.DebugLogWindow;
import ch.retorte.intervalmusiccompositor.ui.graphics.BarChart;
import ch.retorte.intervalmusiccompositor.ui.updatecheck.UpdateCheckDialog;
import ch.retorte.intervalmusiccompositor.ui.utils.AudioFileEncoderConverter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.CORE_RESOURCE_BUNDLE_NAME;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;

/**
 * Controller for the main screen of the JavaFX ui.
 */
public class MainScreenController implements Initializable {

  //---- FX Widgets

  // Menu

  @FXML
  private MenuItem menuLoadMusicFile;

  @FXML
  private MenuItem menuLoadBreakFile;

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

  // Break track

  @FXML
  private Button addBreakTrackButton;

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
  private Slider blendDuration;

  // File format

  @FXML
  private ChoiceBox<AudioFileEncoder> outputFileFormat;

  // Output directory

  @FXML
  private Button chooseOutputDirectory;

  @FXML
  private Label outputDirectory;

  // Actions

  @FXML
  private Button process;

  // Fields

  private MessageFormatBundle coreBundle = getBundle(CORE_RESOURCE_BUNDLE_NAME);
  private MessageFormatBundle bundle = getBundle(UI_RESOURCE_BUNDLE_NAME);


  private Ui ui;
  private ProgramControl programControl;
  private ApplicationData applicationData;
  private MusicListControl musicListControl;
  private MusicCompilationControl musicCompilationControl;
  private CompilationParameters compilationParameters;
  private MessageProducer messageProducer;
  private UpdateAvailabilityChecker updateAvailabilityChecker;
  private ScheduledExecutorService executorService;
  private List<AudioFileDecoder> audioFileDecoders;

  //---- Methods

  @Override
  public void initialize(URL location, ResourceBundle resourceBundle) {

  }

  public void initializeFieldsWith(Ui ui, ProgramControl programControl, ApplicationData applicationData, MusicListControl musicListControl, MusicCompilationControl musicCompilationControl, CompilationParameters compilationParameters, MessageSubscriber messageSubscriber, MessageProducer messageProducer, UpdateAvailabilityChecker updateAvailabilityChecker, ScheduledExecutorService executorService) {
    this.ui = ui;
    this.programControl = programControl;
    this.applicationData = applicationData;
    this.musicListControl = musicListControl;
    this.musicCompilationControl = musicCompilationControl;
    this.compilationParameters = compilationParameters;
    this.messageProducer = messageProducer;
    this.updateAvailabilityChecker = updateAvailabilityChecker;
    this.executorService = executorService;

    initializeMenu();
    initializeMusicTrackList();
    initializeTrackEnumeration();
    initializeBreakTrackList();
    initializeDurationControls();
    initializeBlending();
    initializeOutputFileFormat();
    initializeOutputDirectory();
    initializeControlButtons();

    addMessageSubscribers(messageSubscriber);
  }

  private void initializeMenu() {
    menuLoadMusicFile.setOnAction(event -> openFileChooserFor(musicTrackListView));
    menuLoadBreakFile.setOnAction(event -> openFileChooserFor(breakTrackListView));
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
    UpdateCheckDialog updateCheckDialog = new UpdateCheckDialog(updateAvailabilityChecker, ui, bundle, coreBundle);
    updateCheckDialog.open();
  }

  private String getAboutWebsiteUrl() {
    return coreBundle.getString("web.website.about.url");
  }



  private void openDebugLog() {
    DebugLogWindow debugLogWindow = new DebugLogWindow(bundle, programControl, executorService);
    debugLogWindow.show();
  }

  private void initializeMusicTrackList() {
    musicTrackListView.initializeWith(musicListControl.getMusicList(), bundle, messageProducer, musicListControl);
    musicTrackListView.addListChangeListener(newValue -> updateUiDataWidgets());
    addMusicTrackButton.setOnAction(event -> openFileChooserFor(musicTrackListView));
  }

  private void initializeTrackEnumeration() {
    sortTrackList.setOnAction(event -> {
      messageProducer.send(new DebugMessage(MainScreenController.this, "Pressed 'sort' button."));
      musicListControl.sortMusicList();
      updateUiDataWidgets();
    });

    shuffleTrackList.setOnAction(event -> {
      messageProducer.send(new DebugMessage(MainScreenController.this, "Pressed 'shuffle' button."));
      musicListControl.shuffleMusicList();
      updateUiDataWidgets();
    });

    trackListSortOrderIndicator.textProperty().addListener(debugHandlerWith(trackListSortOrderIndicator.getId()));

    enumerationToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setEnumerationMode((String) newValue.getUserData()));
    enumerationToggleGroup.selectedToggleProperty().addListener(debugHandlerWith("enumerationToggleGroup"));
    enumerationToggleGroup.selectedToggleProperty().addListener(updateUiHandler());
  }

  private void initializeBreakTrackList() {
    breakTrackListView.initializeWith(musicListControl.getBreakList(), bundle, messageProducer, musicListControl);
    addBreakTrackButton.setOnAction(event -> openFileChooserFor(breakTrackListView));
  }

  private void initializeDurationControls() {
    periodTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (isSimpleTab(newValue)) {
        compilationParameters.setMusicPattern(soundPeriod.getValue());
        compilationParameters.setBreakPattern(breakPeriod.getValue());
      }
      else {
        compilationParameters.setMusicPattern(soundPattern.getText());
        compilationParameters.setBreakPattern(breakPattern.getText());
      }
    });

    soundPeriod.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setMusicPattern(newValue));
    breakPeriod.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setBreakPattern(newValue));
    soundPattern.textProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setMusicPattern(newValue));
    breakPattern.textProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setBreakPattern(newValue));
    iterations.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setIterations(newValue));

    prepareSpinnerForListening(soundPeriod);
    prepareSpinnerForListening(breakPeriod);
    prepareSpinnerForListening(iterations);

    periodTabPane.getSelectionModel().selectedItemProperty().addListener(debugHandlerWith(periodTabPane.getId()));
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
  }

  private void initializeOutputFileFormat() {
    outputFileFormat.valueProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setEncoderIdentifier(newValue.getIdentificator()));
    outputFileFormat.valueProperty().addListener(debugHandlerWith(outputFileFormat.getId()));
  }

  private void initializeOutputDirectory() {
    outputDirectory.textProperty().addListener((observable, oldValue, newValue) -> compilationParameters.setOutputPath(newValue));
    outputDirectory.textProperty().addListener(debugHandlerWith(outputDirectory.getId()));
    chooseOutputDirectory.setOnAction(event -> openDirectoryChooser());
  }

  private void initializeControlButtons() {
    process.setOnAction(event -> musicCompilationControl.startCompilation(compilationParameters));
  }

  private void openDirectoryChooser() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setInitialDirectory(new File(compilationParameters.getOutputPath()));
    directoryChooser.setTitle(bundle.getString("ui.form.outfile_dialog_title"));
    File file = directoryChooser.showDialog(getWindow());
    if (file != null && file.exists()) {
      outputDirectory.setText(file.getAbsolutePath());
    }
  }

  private boolean isSimpleTab(Tab tab) {
    return tab.equals(simpleTab);
  }

  public void updateOutputFileFormatWith(ObservableList<AudioFileEncoder> audioFileEncoders) {
    outputFileFormat.setConverter(new AudioFileEncoderConverter(audioFileEncoders));
    outputFileFormat.setItems(audioFileEncoders);
    outputFileFormat.getSelectionModel().select(0);
  }

  private void prepareSpinnerForListening(Spinner<Integer> spinner) {
    SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();
    TextFormatter<Integer> formatter = new TextFormatter<>(valueFactory.getConverter(), valueFactory.getValue());
    spinner.getEditor().setTextFormatter(formatter);
    valueFactory.valueProperty().bindBidirectional(formatter.valueProperty());
  }

  private void updateDurationEstimation() {
    setDurationTo(getDurationEstimation());
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

  private void updateEnvelopeImage() {
    if (compilationParameters.hasUsableData()) {
      BarChart bar = new BarChart(720, 162);
      bar.generate(compilationParameters.getMusicPattern(), compilationParameters.getBreakPattern(), compilationParameters.getIterations(), false);
      imageView.setImage(bar.getWritableImage());
    }
    else {
      imageView.setImage(starterBackgroundImage);
    }
  }

  private void updateSortModeLabel() {
    trackListSortOrderIndicator.setText(getLabelFor(compilationParameters.getListSortMode()));
  }

  private String getLabelFor(ListSortMode listSortMode) {
    switch (listSortMode) {
      case MANUAL:
        return bundle.getString("ui.form.music_list.list_mode.manual");
      case SHUFFLE:
        return bundle.getString("ui.form.music_list.list_mode.shuffle");
      case SORT:
        return bundle.getString("ui.form.music_list.list_mode.sort");
      case SORT_REV:
        return bundle.getString("ui.form.music_list.list_mode.sort_rev");
      default:
        return "";
    }
  }

  public void setEnvelopeImage(WritableImage envelopeImage) {
    imageView.setImage(envelopeImage);
  }

  private void openFileChooserFor(DraggableAudioFileListView listView) {
    MusicFileChooser musicFileChooser = new MusicFileChooser(messageProducer, bundle, audioFileDecoders);
    List<File> chosenFile = musicFileChooser.chooseFileIn(getWindow());
    chosenFile.forEach(file -> {
      IAudioFile newTrack = listView.addTrack(file);
      newTrack.addChangeListener(newValue -> Platform.runLater(() -> updateTrackCount()));
    });
  }

  private Window getWindow() {
    return container.getScene().getWindow();
  }

  public void updateAvailableDecodersWith(List<AudioFileDecoder> audioFileDecoders) {
    this.audioFileDecoders = audioFileDecoders;
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
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setResizable(true);
      alert.setTitle(bundle.getString("ui.error.title"));
      alert.setContentText(message + "\n\n" + bundle.getString("ui.error.appendix"));
      alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
      alert.show();
    });
  }

  private void updateProgressBar(Integer progressInPercent, String currentActivity) {
    progressBar.progressProperty().setValue((double) progressInPercent / 100);
    secondaryProgressBar.setVisible(false);
  }

  private void updateSubProgressBar(int percentage) {
    secondaryProgressBar.setVisible(true);
    secondaryProgressBar.progressProperty().setValue((double) percentage / 100);
  }

  private DebugMessageEventHandler debugHandlerWith(String id) {
    return new DebugMessageEventHandler(id, messageProducer);
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
  }

  private void updateTrackCount() {
    int tracks = musicListControl.getOkTracks();
    String labelText = bundle.getString("ui.form.music_list.tracks_label_pl", tracks);
    if (tracks == 1) {
      labelText = bundle.getString("ui.form.music_list.tracks_label_sg", tracks);
    }
    trackCount.setText(labelText);
  }

  //---- Inner classes

  private class DebugMessageEventHandler implements ChangeListener<Object> {

    private String id;
    private MessageProducer messageProducer;

    DebugMessageEventHandler(String id, MessageProducer messageProducer) {
      this.id = id;
      this.messageProducer = messageProducer;
    }

    @Override
    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
      messageProducer.send(new DebugMessage(MainScreenController.this, "Changed contents of field '" + id + "' from: '" + oldValue + "' to '" + newValue + "'"));
    }
  }

  private class UpdateDependentUiWidgetsHandler implements ChangeListener<Object> {
    @Override
    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
      updateUiDataWidgets();
    }
  }


}
