package ch.retorte.intervalmusiccompositor.ui.firststart;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.preferences.UserPreferences;
import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
  private Text recentChanges;
  
  @FXML
  private VBox updateSettingsContainer;
  
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
    recentChanges.setText("Test");
    
    updateSettingsContainer.setVisible(hasUnrevisedPreferences());
    
    checkForUpgradesOnStartupPreference.selectedProperty().setValue(userPreferences.loadSearchUpdateAtStartup());
    checkForUpgradesOnStartupPreference.selectedProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSearchUpdateAtStartup(newValue));

    dismissButton.setOnAction(e -> {
      userPreferences.setDidReviseUpdateAtStartup();
      stage.close();
      releaseStage();
    });
  }

  private boolean hasUnrevisedPreferences() {
    return !userPreferences.didReviseUpdateAtStartup();
    
  }

  public void show() {
    stage = new Stage();
    stage.setTitle(bundle.getString("ui.firstStartWindow.title"));
    stage.setScene(new Scene(parent, 600, 600));
    stage.setResizable(true);

    stage.show();

    parent.layout();

    stage.setMinWidth(stage.getWidth());
    stage.setMinHeight(stage.getHeight());

    stage.setOnCloseRequest(event -> {
      releaseStage();
    });

    blockStage();
  }

  private void blockStage() {
    Toolkit.getToolkit().enterNestedEventLoop(stage);
  }

  private void releaseStage() {
    Toolkit.getToolkit().exitNestedEventLoop(stage, null);
  }

}