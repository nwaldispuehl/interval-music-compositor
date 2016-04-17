package ch.retorte.intervalmusiccompositor.ui.mainscreen;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ResourceBundle;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.RESOURCE_BUNDLE_NAME;

/**
 * Controller for the main screen of the JavaFX ui.
 */
public class MainScreenController implements Initializable {

  private MessageFormatBundle bundle = getBundle(RESOURCE_BUNDLE_NAME);

  @FXML
  private Label musicTrackLabel;

  @FXML
  private Label breakTrackLabel;

  @FXML
  private Button sortTrackList;

  @FXML
  private Button shuffleTrackList;

  @FXML
  private Label trackListSortOrderIndicator;

  @FXML
  private ListView<IAudioFile> musicTrackListView;

  @FXML
  private ListView<IAudioFile> breakTrackListView;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setSortModeLabelTo(bundle.getString("ui.form.music_list.list_mode.sort"));
  }

  private void setSortModeLabelTo(String sortMode) {
    trackListSortOrderIndicator.setText(bundle.getString("ui.form.music_list.list_mode.prefix") + " " + sortMode);
  }

}
