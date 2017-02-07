package ch.retorte.intervalmusiccompositor.ui.updatecheck;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Dialog which immediately starts a version check.
 */
public class UpdateCheckDialog {

  //---- Fields

  private UpdateAvailabilityChecker updateAvailabilityChecker;
  private Ui ui;
  private MessageFormatBundle bundle;
  private MessageFormatBundle coreBundle;
  private Alert alert;


  //---- Constructor

  public UpdateCheckDialog(UpdateAvailabilityChecker updateAvailabilityChecker, Ui ui, MessageFormatBundle bundle, MessageFormatBundle coreBundle) {
    this.updateAvailabilityChecker = updateAvailabilityChecker;
    this.ui = ui;
    this.bundle = bundle;
    this.coreBundle = coreBundle;
    createAlert();
  }


  //---- Methods

  public void open() {
    alert.show();
    startVersionCheck();
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

  private void updateAlertPaneWith(Node node) {
    Platform.runLater(() -> {
      alert.getDialogPane().setContent(node);
      alert.getDialogPane().requestLayout();
      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      stage.sizeToScene();
    });
  }

  private void startVersionCheck() {
    // We are creating a thread executor, but shutting it down right away so it gets collected once the task finishes.
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(new VersionCheckTask());
    executorService.shutdown();
  }

  private Hyperlink downloadHyperlink() {
    String downloadText = bundle.getString("ui.about.update.message.download");
    String downloadUrl = coreBundle.getString("web.download.url");

    Hyperlink hyperlink = new Hyperlink(downloadText);
    hyperlink.setOnAction((actionEvent) -> ui.openInDesktopBrowser(downloadUrl));

    return hyperlink;
  }

  //---- Inner classes

  private class VersionCheckTask extends Task<String> {

    @Override
    protected String call() throws Exception {
      try {
        boolean updateAvailable = updateAvailabilityChecker.isUpdateAvailable();
        if (updateAvailable) {
          Version latestVersion = updateAvailabilityChecker.getLatestVersion();
          String latestVersionText = bundle.getString("ui.about.update.message.new", latestVersion);

          VBox vBox = new VBox(20);
          vBox.getChildren().add(new Text(latestVersionText));
          vBox.getChildren().add(downloadHyperlink());

          updateAlertPaneWith(vBox);

        } else {
          updateAlertTextWith(bundle.getString("ui.about.update.message.latest"));
        }
      }
      catch (Exception e) {
        updateAlertTextWith(bundle.getString("ui.about.update.message.problem"));
      }
      return null;
    }
  }

}
