package ch.retorte.intervalmusiccompositor.ui.updatecheck;

import ch.retorte.intervalmusiccompositor.commons.VersionChecker;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.spi.update.VersionUpgrader;
import ch.retorte.intervalmusiccompositor.ui.preferences.PreferencesWindow;
import ch.retorte.intervalmusiccompositor.ui.preferences.UiUserPreferences;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static javafx.util.Duration.millis;


/**
 * Dialog which can start a version check and show version check results.
 */
public class UpdateCheckDialog {

    //---- Constants

    private static final String RED_TEXT_COLOR_STYLE = "-fx-text-fill: red;";


    //---- Fields

    private final Ui ui;
    private final MessageFormatBundle bundle;
    private final MessageFormatBundle coreBundle;

    private final UiUserPreferences userPreferences;
    private final ApplicationData applicationData;
    private final VersionUpgrader versionUpgrader;
    private final VersionChecker versionChecker;
    private final MessageProducer messageProducer;

    private final Pane messageArea = new Pane();

    private Alert alert;


    //---- Constructor

    public UpdateCheckDialog(UpdateAvailabilityChecker updateAvailabilityChecker, Ui ui, MessageFormatBundle bundle, MessageFormatBundle coreBundle, UiUserPreferences userPreferences, ApplicationData applicationData, VersionUpgrader versionUpgrader, MessageProducer messageProducer) {
        this.ui = ui;
        this.bundle = bundle;
        this.coreBundle = coreBundle;
        this.userPreferences = userPreferences;
        this.applicationData = applicationData;
        this.versionUpgrader = versionUpgrader;
        this.messageProducer = messageProducer;

        versionChecker = new VersionChecker(updateAvailabilityChecker);

        createAlert();
        openAlert();
    }


    //---- Methods

    public void startVersionCheck() {
        versionChecker.startVersionCheckWith(
            this::foundNewVersion,
            nothing -> updateAlertTextWith(bundle.getString("ui.update.message.latest")),
            exception -> updateAlertTextWith(bundle.getString("ui.update.message.problem")));
    }

    private void createAlert() {
        alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.CLOSE);

        alert.setTitle(bundle.getString("ui.update.title"));
        alert.setHeaderText(bundle.getString("ui.update.head"));

        alert.setHeight(600);
        alert.setResizable(true);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/MainScreen.css").toExternalForm());

        updateAlertTextWith(bundle.getString("ui.update.message.pending"));
    }

    private void updateAlertTextWith(String text) {
        Platform.runLater(() -> alert.setContentText(text));
    }

    private void openAlert() {
        alert.show();
    }

    public void foundNewVersion(Version newVersion) {
        String latestVersionText = bundle.getString("ui.update.message.new", newVersion);

        VBox vBox = new VBox(20);
        vBox.getChildren().add(new Text(latestVersionText));

        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        hBox.getChildren().add(downloadButton());
        hBox.getChildren().add(updateButtonWith(newVersion));

        vBox.getChildren().add(messageArea);

        vBox.getChildren().add(checkForUpdateOnStartupCheckbox());

        updateAlertPaneWith(vBox);
    }

    private void updateAlertPaneWith(Parent parent) {
        Platform.runLater(() -> {
            alert.getDialogPane().setContent(parent);
            parent.requestLayout();
            alert.getDialogPane().requestLayout();
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.sizeToScene();
        });
    }

    private Button downloadButton() {
        String downloadText = bundle.getString("ui.update.message.download");
        String downloadUrl = coreBundle.getString("web.download.url");

        Button downloadButton = new Button(downloadText);
        downloadButton.setTooltip(tooltipWith(downloadUrl));
        downloadButton.setOnAction((actionEvent) -> ui.openInDesktopBrowser(downloadUrl));

        return downloadButton;
    }

    private Tooltip tooltipWith(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(millis(200));
        return tooltip;
    }

    private Button updateButtonWith(Version version) {
        String updateText = bundle.getString("ui.update.message.update");
        Button updateButton = new Button(updateText);
        updateButton.setOnAction(actionEvent -> {
            updateButton.setDisable(true);
            updateSoftwareTo(version);
        });

        return updateButton;
    }

    private void updateSoftwareTo(Version version) {
        final Label message = new Label();
        messageArea.getChildren().add(message);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                try {
                    versionUpgrader.upgradeTo(version, new ProgressListener() {
                        @Override
                        public void onProgressUpdate(String progress) {
                            messageProducer.send(new DebugMessage(versionUpgrader, progress));
                            Platform.runLater(() -> message.setText(progress));
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        message.setStyle(RED_TEXT_COLOR_STYLE);
                        message.setText(e.getMessage());
                    });
                    throw e;
                }

                return null;
            }
        };

        new Thread(task).start();
    }

    private Node checkForUpdateOnStartupCheckbox() {
        HBox result = new HBox(15);
        result.paddingProperty().setValue(new Insets(10, 0, 0, 0));

        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(userPreferences.searchUpdateAtStartupProperty());
        result.getChildren().add(checkBox);

        Label label = new Label(bundle.getString("ui.update.message.checkOnStartup"));
        label.getStyleClass().add("legend");
        result.getChildren().add(label);

        Hyperlink preferencesLink = new Hyperlink(bundle.getString("ui.menu.file.preferences"));
        preferencesLink.setOnAction((actionEvent) -> new PreferencesWindow(bundle, userPreferences, applicationData).show());
        preferencesLink.getStyleClass().add("legend");
        result.getChildren().add(preferencesLink);

        return result;
    }

}
