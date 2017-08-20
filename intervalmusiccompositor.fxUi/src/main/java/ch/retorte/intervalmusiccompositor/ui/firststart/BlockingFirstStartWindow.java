package ch.retorte.intervalmusiccompositor.ui.firststart;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.preferences.UserPreferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.IOException;

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
  private Button dismissButton;

  private Parent parent;
  private Stage stage;
  private MessageFormatBundle bundle;
  private UserPreferences userPreferences;


  //---- Constructor

  public BlockingFirstStartWindow(MessageFormatBundle bundle, UserPreferences userPreferences) {
    this.bundle = bundle;
    this.userPreferences = userPreferences;

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
    checkForUpgradesOnStartupPreference.selectedProperty().setValue(userPreferences.loadSearchUpdateAtStartup());
    checkForUpgradesOnStartupPreference.selectedProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSearchUpdateAtStartup(newValue));

    dismissButton.setOnAction(e -> stage.close());
  }

  public void show() {
    stage = new Stage();
    stage.setTitle(bundle.getString("ui.firstStartWindow.title"));
    stage.setScene(new Scene(parent, 600, 300));
    stage.setResizable(true);

    // We want this window to block until it is closed.
    stage.showAndWait();
  }

}
