package ch.retorte.intervalmusiccompositor.ui.updatecheck;

import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.VersionChecker;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.ui.preferences.PreferencesWindow;
import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Dialog which can start a version check and show version check results.
 */
public class UpdateCheckDialog {

  //---- Fields

  private Ui ui;
  private MessageFormatBundle bundle;
  private MessageFormatBundle coreBundle;

  private VersionChecker versionChecker;
  private final UiUserPreferences userPreferences;
  private final ApplicationData applicationData;

  private Alert alert;


  //---- Constructor

  public UpdateCheckDialog(UpdateAvailabilityChecker updateAvailabilityChecker, Ui ui, MessageFormatBundle bundle, MessageFormatBundle coreBundle, UiUserPreferences userPreferences, ApplicationData applicationData) {
    this.ui = ui;
    this.bundle = bundle;
    this.coreBundle = coreBundle;
    this.userPreferences = userPreferences;
    this.applicationData = applicationData;

    versionChecker = new VersionChecker(updateAvailabilityChecker);

    createAlert();
    openAlert();
  }


  //---- Methods

  public void startVersionCheck() {
    versionChecker.startVersionCheckWith(
        this::foundNewVersion,
        nothing -> updateAlertTextWith(bundle.getString("ui.about.update.message.latest")),
        exception -> updateAlertTextWith(bundle.getString("ui.about.update.message.problem")));
  }

  private void createAlert() {
    alert = new Alert(Alert.AlertType.INFORMATION);

    alert.setTitle(bundle.getString("ui.about.update.title"));
    alert.setHeaderText(bundle.getString("ui.about.update.head"));

    alert.setHeight(600);
    alert.setResizable(true);

    alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/MainScreen.css").toExternalForm());

    updateAlertTextWith(bundle.getString("ui.about.update.message.pending"));
  }

  private void updateAlertTextWith(String text) {
    Platform.runLater(() -> alert.setContentText(text));
  }

  private void openAlert() {
    alert.show();
  }

  public void foundNewVersion(Version newVersion) {
    String latestVersionText = bundle.getString("ui.about.update.message.new", newVersion);

    VBox vBox = new VBox(20);
    vBox.getChildren().add(new Text(latestVersionText));
    vBox.getChildren().add(downloadHyperlink());
    vBox.getChildren().add(checkForUpdateOnStartupCheckbox());

    updateAlertPaneWith(vBox);
  }

  private void updateAlertPaneWith(Node node) {
    Platform.runLater(() -> {
      alert.getDialogPane().setContent(node);
      alert.getDialogPane().requestLayout();
      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      stage.sizeToScene();
    });
  }

  private Hyperlink downloadHyperlink() {
    String downloadText = bundle.getString("ui.about.update.message.download");
    String downloadUrl = coreBundle.getString("web.download.url");

    Hyperlink downloadLink = new Hyperlink(downloadText);
    downloadLink.setOnAction((actionEvent) -> ui.openInDesktopBrowser(downloadUrl));

    return downloadLink;
  }

  private Node checkForUpdateOnStartupCheckbox() {
    HBox result = new HBox(15);
    result.paddingProperty().setValue(new Insets(10, 0, 0, 0));

    CheckBox checkBox = new CheckBox();
    checkBox.selectedProperty().bindBidirectional(userPreferences.searchUpdateAtStartupProperty());
    result.getChildren().add(checkBox);

    Label label = new Label(bundle.getString("ui.about.update.message.checkOnStartup"));
    label.getStyleClass().add("legend");
    result.getChildren().add(label);

    Hyperlink preferencesLink = new Hyperlink(bundle.getString("ui.menu.file.preferences"));
    preferencesLink.setOnAction((actionEvent) -> new PreferencesWindow(bundle, userPreferences, applicationData).show());
    preferencesLink.getStyleClass().add("legend");
    result.getChildren().add(preferencesLink);

    return result;
  }

}
