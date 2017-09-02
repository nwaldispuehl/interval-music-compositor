package ch.retorte.intervalmusiccompositor.ui.updatecheck;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.VersionChecker;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
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

  private Alert alert;


  //---- Constructor

  public UpdateCheckDialog(UpdateAvailabilityChecker updateAvailabilityChecker, Ui ui, MessageFormatBundle bundle, MessageFormatBundle coreBundle) {
    this.ui = ui;
    this.bundle = bundle;
    this.coreBundle = coreBundle;

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

    Hyperlink hyperlink = new Hyperlink(downloadText);
    hyperlink.setOnAction((actionEvent) -> ui.openInDesktopBrowser(downloadUrl));

    return hyperlink;
  }


}
