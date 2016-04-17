package ch.retorte.intervalmusiccompositor.ui;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.Utf8Control;
import ch.retorte.intervalmusiccompositor.spi.*;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.ui.mainscreen.MainScreenController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.ResourceBundle;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

/**
 * A ui based on JavaFx.
 */
public class IntervalMusicCompositorUI extends Application implements Ui {

  //---- Static

  private static final String MAIN_SCREEN_LAYOUT_FILE = "/layouts/MainScreen.fxml";
  public static final String RESOURCE_BUNDLE_NAME = "ui_imc";

  //---- Fields

  private MessageFormatBundle bundle = getBundle(RESOURCE_BUNDLE_NAME);

  private static MusicListControl musicListControl;
  private static MusicCompilationControl musicCompilationController;
  private static ProgramControl programControl;
  private static ApplicationData applicationData;
  private static UpdateAvailabilityChecker updateAvailabilityChecker;
  private static MessageSubscriber messageSubscriber;
  private static MessageProducer messageProducer;

  //---- Constructors

  public IntervalMusicCompositorUI() { /* Default constructor needed by JavaFX Application. */ }

  public IntervalMusicCompositorUI(final MusicListControl musicListControl,
                                   final MusicCompilationControl musicCompilationController,
                                   final ProgramControl programControl,
                                   ApplicationData applicationData,
                                   UpdateAvailabilityChecker updateAvailabilityChecker,
                                   MessageSubscriber messageSubscriber,
                                   MessageProducer messageProducer) {
    this.musicListControl = musicListControl;
    this.musicCompilationController = musicCompilationController;
    this.programControl = programControl;
    this.applicationData = applicationData;
    this.updateAvailabilityChecker = updateAvailabilityChecker;
    this.messageSubscriber = messageSubscriber;
    this.messageProducer = messageProducer;
  }

  //---- Methods

  @Override
  public void start(Stage primaryStage) throws Exception {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, new Utf8Control());
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_SCREEN_LAYOUT_FILE), resourceBundle);

    Parent root = fxmlLoader.load();



    primaryStage.setTitle(applicationData.getProgramName());
    primaryStage.setScene(new Scene(root));
    primaryStage.show();

    addProgramIconsTo(primaryStage);

    MainScreenController controller = fxmlLoader.getController();
  }

  @Override
  public void quit() {
    programControl.quit();
    Platform.exit();
  }

  @Override
  public void launch() {
    Application.launch();
    Platform.runLater(() -> {
      try {
        start(new Stage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

  }

  private void addProgramIconsTo(Stage stage) {
    stage.getIcons().add(new Image("file:images/program_icon.png"));
    stage.getIcons().add(new Image("file:images/program_icon_small.png"));
  }

  @Override
  public void setActive() {

  }

  @Override
  public void setInactive() {

  }

  @Override
  public void setEnvelopeImage(BufferedImage envelopeImage) {

  }

  @Override
  public void refresh() {

  }

  @Override
  public void updateUsableTracks() {

  }
}
