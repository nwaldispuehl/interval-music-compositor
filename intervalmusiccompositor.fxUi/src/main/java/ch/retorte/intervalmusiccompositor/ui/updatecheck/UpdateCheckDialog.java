package ch.retorte.intervalmusiccompositor.ui.updatecheck;

import ch.retorte.intervalmusiccompositor.Version;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.concurrent.Executors;


/**
 * Dialog which immediately starts a version check.
 */
public class UpdateCheckDialog {

  private UpdateAvailabilityChecker updateAvailabilityChecker;
  private MessageFormatBundle bundle;
  private Alert alert;

  private SimpleStringProperty versionMessage = new SimpleStringProperty();

  public UpdateCheckDialog(UpdateAvailabilityChecker updateAvailabilityChecker, MessageFormatBundle bundle) {
    this.updateAvailabilityChecker = updateAvailabilityChecker;
    this.bundle = bundle;
    createAlert();
  }

  private void createAlert() {
    alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(bundle.getString("ui.about.update.title"));
    alert.setHeaderText(bundle.getString("ui.about.update.head"));
    alert.contentTextProperty().bind(versionMessage);

    versionMessage.set(bundle.getString("ui.about.update.message.pending"));
  }

  public void open() {
    alert.show();
    startVersionCheck();
  }

  private void startVersionCheck() {
    Executors.newSingleThreadExecutor().submit(new VersionCheckTask());
  }

  private class VersionCheckTask extends Task<String> {

    @Override
    protected String call() throws Exception {
      try {
        boolean updateAvailable = updateAvailabilityChecker.isUpdateAvailable();
        if (updateAvailable) {
          Version latestVersion = updateAvailabilityChecker.getLatestVersion();
          setVersionMessageTo(bundle.getString("ui.about.update.message.new", latestVersion));
        } else {
          setVersionMessageTo(bundle.getString("ui.about.update.message.latest"));
        }
      }
      catch (Exception e) {
        setVersionMessageTo(bundle.getString("ui.about.update.message.problem"));
      }
      return null;
    }

    private void setVersionMessageTo(String message) {
      Platform.runLater(() -> versionMessage.set(message));
    }
  }

}
