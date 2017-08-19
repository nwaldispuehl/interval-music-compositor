package ch.retorte.intervalmusiccompositor.ui.preferences;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * Window holding the program preferences.
 */
public class PreferencesWindow {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/PreferencesWindow.fxml";


  //---- Fields

  @FXML
  private Button closeButton;

  @FXML
  private ChoiceBox<Locale> languagePreference;

  @FXML
  private CheckBox propertyStoragePreference;

  @FXML
  private CheckBox startupUpdateSearchPreference;

  private Parent parent;
  private final MessageFormatBundle bundle;
  private UiUserPreferences userPreferences;
  private ApplicationData applicationData;


  //---- Constructor

  public PreferencesWindow(MessageFormatBundle bundle, UiUserPreferences userPreferences, ApplicationData applicationData) {
    this.bundle = bundle;
    this.userPreferences = userPreferences;
    this.applicationData = applicationData;

    loadFXML();
    bindButton();
    initializePreferenceFields();
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

  private void bindButton() {
    closeButton.setOnAction(event -> hide());
  }

  private void hide() {
    parent.getScene().getWindow().hide();
  }

  private void initializePreferenceFields() {
    languagePreference.setItems(observableArrayList(applicationData.getKnownLocales()));
    languagePreference.setValue(applicationData.getLocale());
    languagePreference.valueProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveLocale(newValue));

    propertyStoragePreference.selectedProperty().setValue(userPreferences.loadSaveFieldState());
    propertyStoragePreference.selectedProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSaveFieldState(newValue));

    startupUpdateSearchPreference.selectedProperty().setValue(userPreferences.loadSearchUpdateAtStartup());
    startupUpdateSearchPreference.selectedProperty().addListener((observable, oldValue, newValue) -> userPreferences.saveSearchUpdateAtStartup(newValue));
  }

  public void show() {
    Stage stage = new Stage();
    stage.setTitle(bundle.getString("ui.preferences.title"));
    stage.setScene(new Scene(parent, 800, 300));
    stage.setResizable(true);
    stage.show();

    stage.setMinWidth(stage.getWidth());
    stage.setMinHeight(stage.getHeight());
  }



}
