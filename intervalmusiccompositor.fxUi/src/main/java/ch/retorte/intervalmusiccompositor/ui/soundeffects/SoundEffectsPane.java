package ch.retorte.intervalmusiccompositor.ui.soundeffects;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import com.sun.deploy.uitoolkit.impl.fx.ui.FXUIFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.ui.IntervalMusicCompositorUI.UI_RESOURCE_BUNDLE_NAME;

public class SoundEffectsPane extends BorderPane {

  //---- Static

  private static final String LAYOUT_FILE = "/layouts/SoundEffectsPane.fxml";


  //---- Fields

  private MessageFormatBundle bundle = getBundle(UI_RESOURCE_BUNDLE_NAME);
  private SoundEffectsProvider soundEffectsProvider;


  //---- FX fields

  @FXML
  private CheckBox addSoundEffects;

  @FXML
  private HBox soundEffectsContainer;

  @FXML
  private ComboBox<SoundEffect> soundEffects;

  //---- Constructor

  public SoundEffectsPane(SoundEffectsProvider soundEffectsProvider) {
    this.soundEffectsProvider = soundEffectsProvider;
    initialize();
    initializeFields();
    soundEffectsContainer.setVisible(true);
  }

  private void initialize() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LAYOUT_FILE), bundle.getBundle());
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception);
    }
  }

  private void initializeFields() {
    soundEffects.setItems(FXCollections.observableArrayList(soundEffectsProvider.getSoundEffects()));
  }

  //---- Methods


}
