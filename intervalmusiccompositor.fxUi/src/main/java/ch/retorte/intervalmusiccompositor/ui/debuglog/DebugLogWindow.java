package ch.retorte.intervalmusiccompositor.ui.debuglog;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.spi.ProgramControl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The debug log window shows the current debug log in a text area.
 */
public class DebugLogWindow {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/DebugLogWindow.fxml";

  //---- Fields

  @FXML
  private TextArea logField;

  @FXML
  private Button closeButton;

  private Parent parent;
  private MessageFormatBundle messageFormatBundle;
  private ProgramControl programControl;
  private ScheduledExecutorService executorService;


  private AtomicBoolean recentUpdate = new AtomicBoolean(false);

  //---- Constructor

  public DebugLogWindow(MessageFormatBundle messageFormatBundle, ProgramControl programControl, ScheduledExecutorService executorService) {
    this.messageFormatBundle = messageFormatBundle;
    this.programControl = programControl;
    this.executorService = executorService;

    loadFXML();
    bindLogData();
    bindButton();
  }

  private void loadFXML() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LAYOUT_FILE), messageFormatBundle.getBundle());
    fxmlLoader.setController(this);

    try {
      parent = fxmlLoader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void bindLogData() {
    logField.setText(programControl.getLogBuffer().toString());
    programControl.getLogBuffer().addObserver((logBuffer, arg) -> {
      if (recentUpdate.compareAndSet(false, true)) {
        updateLogContent();
        scheduleFlagUpdate();
      }
    });
  }

  private void scheduleFlagUpdate() {
    if (!executorService.isTerminated()) {
      executorService.schedule(() -> {
        recentUpdate.set(false);
        updateLogContent();
      }, 1, TimeUnit.SECONDS);
    }
  }

  private void updateLogContent () {
    Platform.runLater(() -> {
      logField.setText(programControl.getLogBuffer().toString() + "\n\n");
      logField.setScrollTop(Double.MAX_VALUE);
    });
  }

  private void bindButton() {
    closeButton.setOnAction(event -> hide());
  }

  public void show() {
    Stage stage = new Stage();
    stage.setTitle(messageFormatBundle.getString("ui.debugLog.title"));
    stage.setScene(new Scene(parent, 800, 600));
    stage.setResizable(true);
    stage.show();
  }

  private void hide() {
    parent.getScene().getWindow().hide();
  }

}

