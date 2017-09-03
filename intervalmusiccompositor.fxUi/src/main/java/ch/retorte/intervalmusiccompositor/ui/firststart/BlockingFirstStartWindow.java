package ch.retorte.intervalmusiccompositor.ui.firststart;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.preferences.UserPreferences;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import com.google.common.base.Joiner;
import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Window shown on the first start of the program; contains welcome message, recent updates and important preferences.
 */
public class BlockingFirstStartWindow {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/FirstStartWindow.fxml";

  //---- Fields

  @FXML
  private CheckBox checkForUpgradesOnStartupPreference;

  @FXML
  private Label recentChanges;
  
  @FXML
  private VBox updateSettingsContainer;
  
  @FXML
  private Button dismissButton;

  private Parent parent;
  private Stage stage;
  private MessageFormatBundle bundle;
  private UserPreferences userPreferences;
  private ApplicationData applicationData;


  //---- Constructor

  public BlockingFirstStartWindow(MessageFormatBundle bundle, UserPreferences userPreferences, ApplicationData applicationData) {
    this.bundle = bundle;
    this.userPreferences = userPreferences;
    this.applicationData = applicationData;

    loadFXML();
    initializeControls();
  }


  //---- Methods

  private void loadFXML() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LAYOUT_FILE), bundle.getBundle());
    fxmlLoader.setController(this);

    try {
      parent = fxmlLoader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void initializeControls() {
    recentChanges.setText(removeFirstNLinesOf(3, applicationData.getChangeLog()));

    /* For the node to be completely removed from the layout if hidden, we need to adapt its managed property accordingly. */
    updateSettingsContainer.managedProperty().bind(updateSettingsContainer.visibleProperty());
    updateSettingsContainer.setVisible(hasUnrevisedPreferences());

    checkForUpgradesOnStartupPreference.selectedProperty().setValue(userPreferences.loadSearchUpdateAtStartup());
    checkForUpgradesOnStartupPreference.selectedProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSearchUpdateAtStartup(newValue));

    dismissButton.setOnAction(e -> {
      userPreferences.setDidReviseUpdateAtStartup();
      stage.close();
      releaseStage();
    });
  }

  /**
   * The first lines of the change log are taken by the title. We thus cut them away.
   */
  private String removeFirstNLinesOf(int n, String changeLog) {
    if (StringUtils.isBlank(changeLog)) {
      return "?";
    }
    String[] lines = changeLog.split(System.lineSeparator());

    if (3 < lines.length) {
      return Joiner.on(System.lineSeparator()).join(Arrays.copyOfRange(lines, n, lines.length - 1));
    }
    else {
      return changeLog;
    }
  }

  private boolean hasUnrevisedPreferences() {
    return !userPreferences.didReviseUpdateAtStartup();
    
  }

  public void show() {
    stage = new Stage();
    stage.setTitle(bundle.getString("ui.firstStartWindow.title"));
    stage.setScene(new Scene(parent));
    stage.setResizable(true);

    stage.show();

    parent.layout();

    stage.setMinWidth(stage.getWidth());
    stage.setMinHeight(stage.getHeight());

    stage.setOnCloseRequest(event -> releaseStage());
    blockStage();
  }

  private void blockStage() {
    Toolkit.getToolkit().enterNestedEventLoop(stage);
  }

  private void releaseStage() {
    Toolkit.getToolkit().exitNestedEventLoop(stage, null);
  }

}
