package ch.retorte.intervalmusiccompositor.ui;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.*;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.ui.firststart.BlockingFirstStartWindow;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.MainScreenController;
import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;


/**
 * A ui based on JavaFx.
 */
public class IntervalMusicCompositorUI extends Application implements Ui {

  //---- Static

  private static final String MAIN_SCREEN_LAYOUT_FILE = "/layouts/MainScreen.fxml";
  public static final String CORE_RESOURCE_BUNDLE_NAME = "core_imc";
  public static final String UI_RESOURCE_BUNDLE_NAME = "ui_imc";

  private static MusicListControl musicListControl;
  private static MusicCompilationControl musicCompilationController;
  private static ProgramControl programControl;
  private static ApplicationData applicationData;
  private static UpdateAvailabilityChecker updateAvailabilityChecker;
  private static SoundEffectsProvider soundEffectsProvider;
  private static UiUserPreferences userPreferences;
  private static MessageSubscriber messageSubscriber;
  private static MessageProducer messageProducer;
  private static MainScreenController mainScreenController;


  //---- Fields

  private CompilationParameters compilationParameters = new CompilationParameters();

  private MessageFormatBundle bundle = getBundle(UI_RESOURCE_BUNDLE_NAME);

  private ch.retorte.intervalmusiccompositor.commons.platform.Platform platform = new PlatformFactory().getPlatform();

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

  //---- Constructors

  public IntervalMusicCompositorUI() { /* Default constructor needed by JavaFX Application. */ }

  public IntervalMusicCompositorUI(final MusicListControl musicListControl,
                                   final MusicCompilationControl musicCompilationController,
                                   final ProgramControl programControl,
                                   ApplicationData applicationData,
                                   UpdateAvailabilityChecker updateAvailabilityChecker,
                                   SoundEffectsProvider soundEffectsProvider,
                                   UiUserPreferences userPreferences,
                                   MessageSubscriber messageSubscriber,
                                   MessageProducer messageProducer) {
    IntervalMusicCompositorUI.musicListControl = musicListControl;
    IntervalMusicCompositorUI.musicCompilationController = musicCompilationController;
    IntervalMusicCompositorUI.programControl = programControl;
    IntervalMusicCompositorUI.applicationData = applicationData;
    IntervalMusicCompositorUI.updateAvailabilityChecker = updateAvailabilityChecker;
    IntervalMusicCompositorUI.soundEffectsProvider = soundEffectsProvider;
    IntervalMusicCompositorUI.userPreferences = userPreferences;
    IntervalMusicCompositorUI.messageSubscriber = messageSubscriber;
    IntervalMusicCompositorUI.messageProducer = messageProducer;

    installUncaughtExceptionHandler();
  }

  //---- Methods

  private void installUncaughtExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      messageProducer.send(new ErrorMessage(throwable));
      messageProducer.send(new DebugMessage(IntervalMusicCompositorUI.this, throwable));
    });
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Platform.setImplicitExit(true);
    try {
      if (isFirstStart()) {
        addDebugMessage("Showing first start window.");
        startBlockingFirstStartWindow();
      }

      // TODO: Check for update if setting is set
      startMainProgramIn(primaryStage);
    }
    catch (Exception e) {
      messageProducer.send(new ErrorMessage("UI failed to load due to: " + e.getMessage() + " Quitting."));
      messageProducer.send(new DebugMessage(this, e));
      quit();
    }
  }

  /**
   * We show the first start window when there is no stored version or there is one but it is not equal to the current program version.
   */
  private boolean isFirstStart() {
    Version currentProgramVersion = applicationData.getProgramVersion();
    Version lastProgramVersion = userPreferences.loadLastProgramVersion();

    boolean isFirstStart = !currentProgramVersion.equals(lastProgramVersion);

    addDebugMessage("Is this a new version? Current version: " + currentProgramVersion + ", version of previous start: " + lastProgramVersion + ", verdict: " + isFirstStart);
    return isFirstStart;
  }

  private void startBlockingFirstStartWindow() {
    new BlockingFirstStartWindow(bundle, userPreferences).show();
  }

  private void startMainProgramIn(Stage primaryStage) throws Exception {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_SCREEN_LAYOUT_FILE), bundle.getBundle());
    Parent root = fxmlLoader.load();
    primaryStage.setTitle(applicationData.getProgramName());
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
    addProgramIconsTo(primaryStage);

    mainScreenController = fxmlLoader.getController();

    initialize(mainScreenController);
    initializeCompilationParameters();

    userPreferences.saveLastProgramVersion(applicationData.getProgramVersion());
    userPreferences.saveLastStart();
  }

  private void initializeCompilationParameters() {
    compilationParameters.setDefaultOutputPath(platform.getDesktopPath());
    compilationParameters.resetOutputPath();
  }

  @Override
  public void quit() {
    Platform.exit();
  }

  @Override
  public void stop() throws Exception {
    /* Is called right before the platform exits. */
    shutdownExecutorService();
    programControl.quit();
    super.stop();
  }

  private void shutdownExecutorService() {
    try {
      executorService.shutdown();
    }
    catch (Exception e) {
      // nop
    }
  }

  @Override
  public void launch() {
      try {
        Application.launch();
      } catch (Exception e) {
        messageProducer.send(new DebugMessage(getClass().getSimpleName(), "Failed to launch JavaFX application.", e));
      }
  }

  private void addProgramIconsTo(Stage stage) {
    stage.getIcons().add(new Image("file:resources/images/program_icon.png"));
    stage.getIcons().add(new Image("file:resources/images/program_icon_small.png"));
  }

  @Override
  public void setActive() {
    mainScreenController.setActive();
  }

  @Override
  public void setInactive() {
    mainScreenController.setInactive();
  }

  @Override
  public void setEnvelopeImage(WritableImage envelopeImage) {
    mainScreenController.setEnvelopeImage(envelopeImage);
  }

  @Override
  public void openInDesktopBrowser(String url) {
    URI uri = getUriFor(url);
    if (uri != null) {
      addDebugMessage("Sending URL to systems Desktop for opening in web browser: " + uri);
      getHostServices().showDocument(uri.toASCIIString());
    }
  }

  private URI getUriFor(String string) {
    try {
      return new URI(string);
    }
    catch (Exception e) {
      messageProducer.send(new ErrorMessage("Not able to convert help website url to URI due to: " + e.getMessage()));
      messageProducer.send(new DebugMessage(this, e));
    }
    return null;
  }

  private void initialize(MainScreenController mainScreenController) {
    mainScreenController.initializeFieldsWith(this, programControl, applicationData, musicListControl, musicCompilationController, compilationParameters, messageSubscriber, messageProducer, updateAvailabilityChecker, executorService, soundEffectsProvider, getAudioFileDecoderExtensions(), getAudioFileEncoders(), userPreferences);
  }

  private List<AudioFileEncoder> getAudioFileEncoders() {
    return musicCompilationController.getAvailableEncoders();
  }

  private List<AudioFileDecoder> getAudioFileDecoderExtensions() {
    return musicCompilationController.getAvailableDecoders();
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(getClass().getSimpleName(), message));
  }


}
