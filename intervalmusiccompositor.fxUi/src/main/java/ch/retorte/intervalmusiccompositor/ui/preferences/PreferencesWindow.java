package ch.retorte.intervalmusiccompositor.ui.preferences;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;

/**
 * Window holding the program preferences.
 *
 * Currently:
 * - save properties
 * - look for update on startup
 * - language
 *
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


  //---- Constructor

  public PreferencesWindow(MessageFormatBundle bundle, UiUserPreferences userPreferences) {
    this.bundle = bundle;
    this.userPreferences = userPreferences;

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

    // TODO

  }

  public void show() {
    Stage stage = new Stage();
    stage.setTitle(bundle.getString("ui.preferences.title"));
    stage.setScene(new Scene(parent, 800, 400));
    stage.setResizable(true);
    stage.show();
  }



}
