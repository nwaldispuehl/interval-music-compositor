package ch.retorte.intervalmusiccompositor.spi;

import java.awt.image.BufferedImage;

/**
 * @author nw
 */
public interface Ui {

  void quit();

  void launch();

  void showErrorMessage(String message);

  void setActive();

  void setInactive();

  void setEnvelopeImage(BufferedImage envelopeImage);

  // Shouldn't this be managed by the ui itself?
  void refresh();

  void updateUsableTracks();
}
