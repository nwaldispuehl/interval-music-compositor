package ch.retorte.intervalmusiccompositor.ui;

import ch.retorte.intervalmusiccompositor.commons.VersionChecker;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.model.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.*;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.encoder.AudioFileEncoder;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.spi.update.VersionUpgrader;
import ch.retorte.intervalmusiccompositor.ui.bundle.UiBundleProvider;
import ch.retorte.intervalmusiccompositor.ui.firststart.BlockingFirstStartWindow;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.MainScreenController;
import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
import ch.retorte.intervalmusiccompositor.ui.updatecheck.UpdateCheckDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * A ui based on JavaFx.
 */
public class IntervalMusicCompositorUI extends Application implements Ui {

    //---- Static

    private static final String MAIN_SCREEN_LAYOUT_FILE = "/layouts/MainScreen.fxml";

    private static MusicListControl musicListControl;
    private static MusicCompilationControl musicCompilationController;
    private static ProgramControl programControl;
    private static ApplicationData applicationData;
    private static VersionUpgrader versionUpgrader;
    private static UpdateAvailabilityChecker updateAvailabilityChecker;
    private static SoundEffectsProvider soundEffectsProvider;
    private static UiUserPreferences userPreferences;
    private static MessageSubscriber messageSubscriber;
    private static MessageProducer messageProducer;
    private static MainScreenController mainScreenController;


    //---- Fields

    private final CompilationParameters compilationParameters = new CompilationParameters();

    private final MessageFormatBundle bundle = new UiBundleProvider().getBundle();
    private final MessageFormatBundle coreBundle = new CoreBundleProvider().getBundle();

    private final ch.retorte.intervalmusiccompositor.commons.platform.Platform platform = new PlatformFactory().getPlatform();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    //---- Constructors

    public IntervalMusicCompositorUI() { /* Default constructor needed by JavaFX Application. */ }

    public IntervalMusicCompositorUI(final MusicListControl musicListControl,
                                     final MusicCompilationControl musicCompilationController,
                                     final ProgramControl programControl,
                                     ApplicationData applicationData,
                                     VersionUpgrader versionUpgrader,
                                     UpdateAvailabilityChecker updateAvailabilityChecker,
                                     SoundEffectsProvider soundEffectsProvider,
                                     UiUserPreferences userPreferences,
                                     MessageSubscriber messageSubscriber,
                                     MessageProducer messageProducer) {
        IntervalMusicCompositorUI.musicListControl = musicListControl;
        IntervalMusicCompositorUI.musicCompilationController = musicCompilationController;
        IntervalMusicCompositorUI.programControl = programControl;
        IntervalMusicCompositorUI.applicationData = applicationData;
        IntervalMusicCompositorUI.versionUpgrader = versionUpgrader;
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
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(true);
        try {
            if (shouldShowFirstStartWindow()) {
                addDebugMessage("Showing first start window.");
                startBlockingFirstStartWindow();
            }

            if (shouldCheckForNewProgramVersion()) {
                checkForNewProgramVersion();
            }

            startMainProgramIn(primaryStage);
        } catch (Exception e) {
            messageProducer.send(new ErrorMessage("UI failed to load due to: " + e.getMessage() + " Quitting."));
            messageProducer.send(new DebugMessage(this, e));
            quit();
        }
    }

    /**
     * We show the first start window when there is no stored version or there is one but it is not equal to the current program version.
     */
    private boolean shouldShowFirstStartWindow() {
        boolean hasUnrevisedSettings = !userPreferences.didReviseUpdateAtStartup();

        Version currentProgramVersion = applicationData.getProgramVersion();
        Version lastProgramVersion = userPreferences.loadLastProgramVersion();

        boolean shouldShowFirstStartWindow = !currentProgramVersion.equals(lastProgramVersion) || hasUnrevisedSettings;

        addDebugMessage("Should we show the first start window? Current version: " + currentProgramVersion + ", version of previous start: " + lastProgramVersion + ", unrevised settings: " + hasUnrevisedSettings + ", verdict: " + shouldShowFirstStartWindow);
        return shouldShowFirstStartWindow;
    }

    private void startBlockingFirstStartWindow() {
        new BlockingFirstStartWindow(bundle, userPreferences, applicationData).show();
    }

    private boolean shouldCheckForNewProgramVersion() {
        boolean didReviseUpdateAtStartup = userPreferences.didReviseUpdateAtStartup();
        boolean searchUpdateAtStartup = userPreferences.loadSearchUpdateAtStartup();
        boolean shouldCheckForNewProgramVersion = didReviseUpdateAtStartup && searchUpdateAtStartup;

        addDebugMessage("Should we check for a new version? User revised setting: " + didReviseUpdateAtStartup + ", search update at startup setting: " + searchUpdateAtStartup + ", verdict: " + shouldCheckForNewProgramVersion);
        return shouldCheckForNewProgramVersion;
    }

    private void checkForNewProgramVersion() {
        addDebugMessage("Starting version check.");
        new VersionChecker(updateAvailabilityChecker).startVersionCheckWith(
            newVersion -> Platform.runLater(() -> new UpdateCheckDialog(updateAvailabilityChecker, this, bundle, coreBundle, userPreferences, applicationData, versionUpgrader, messageProducer).foundNewVersion(newVersion)),
            nothing -> {
            },
            exception -> messageProducer.send(new DebugMessage(this, "Not able to check version due to: " + exception.getMessage(), exception)));
    }

    private void startMainProgramIn(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_SCREEN_LAYOUT_FILE), bundle.getBundle());
        Parent root = fxmlLoader.load();
        primaryStage.setTitle(applicationData.getProgramName());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.toFront();
        addProgramIconsTo(primaryStage);

        mainScreenController = fxmlLoader.getController();

        initializeCompilationParameters();
        initialize(mainScreenController);

        root.requestLayout();
        root.layout();

        userPreferences.saveLastProgramVersion(applicationData.getProgramVersion());
        userPreferences.saveLastStart();
    }

    private void initializeCompilationParameters() {
        try {
            compilationParameters.setDefaultOutputPath(platform.getDesktopPath());
            compilationParameters.resetOutputPath();
        }
        catch (Error e) {
            // If this error is due to (legacy?) accessibility settings on this computer, just swallow it...
            if (isMissingAssistiveTechnologyError(e)) {
                messageProducer.send(new ErrorMessage("Swallowed an error due to accessibility technology declared in " +
                    "the `$USER_HOME/.accessibility.properties` file. Do you still need it? If not better delete the properties file."));
            }
            else {
                // ... otherwise continue with error handling.
                throw e;
            }
        }
    }

    private boolean isMissingAssistiveTechnologyError(Error e) {
        return e instanceof AWTError
            && e.getCause() != null && e.getCause() instanceof ClassNotFoundException
            && e.getMessage() != null && e.getMessage().startsWith("Assistive Technology not found:");
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
        } catch (Exception e) {
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
    public void setEnvelopeImage(BufferedImage envelopeImage) {
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
        } catch (Exception e) {
            messageProducer.send(new ErrorMessage("Not able to convert help website url to URI due to: " + e.getMessage()));
            messageProducer.send(new DebugMessage(this, e));
        }
        return null;
    }

    @Override
    public void openInMailProgram(String mailUrl) {
        if (Desktop.isDesktopSupported()) {
            URI mailUri = getUriFor(mailUrl);
            if (mailUri != null) {
                addDebugMessage("Sending mail URL to systems Desktop for opening in mail program: " + mailUri);

                // We need to detach this due to this bug: https://bugs.openjdk.java.net/browse/JDK-8267572
                // On Linux it would freeze the application without.
                SwingUtilities.invokeLater(() -> {
                    try {
                        Desktop.getDesktop().mail(mailUri);
                    }
                    catch (IOException e) {
                        messageProducer.send(new ErrorMessage("Not able to send mail due to: " + e.getMessage()));
                        messageProducer.send(new DebugMessage(this, e));
                    }
                });
            }
        }
    }

    private void initialize(MainScreenController mainScreenController) {
        mainScreenController.initializeFieldsWith(this, programControl, applicationData, versionUpgrader, musicListControl, musicCompilationController, compilationParameters, messageSubscriber, messageProducer, updateAvailabilityChecker, executorService, soundEffectsProvider, getAudioFileDecoderExtensions(), getAudioFileEncoders(), userPreferences);
    }

    private List<AudioFileEncoder> getAudioFileEncoders() {
        return musicCompilationController.getAvailableEncoders();
    }

    private List<AudioFileDecoder> getAudioFileDecoderExtensions() {
        return musicCompilationController.getAvailableDecoders();
    }

    private void addDebugMessage(String message) {
        messageProducer.send(new DebugMessage(this, message));
    }


}
