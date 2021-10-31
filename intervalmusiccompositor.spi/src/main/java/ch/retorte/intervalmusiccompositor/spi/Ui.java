package ch.retorte.intervalmusiccompositor.spi;

import java.awt.image.BufferedImage;

/**
 * Interface for top level UI operations.
 */
public interface Ui {

  void quit();

  void launch();

  void setActive();

  void setInactive();

  void setEnvelopeImage(BufferedImage envelopeImage);

  void openInDesktopBrowser(String url);

  void openInMailProgram(String mailUrl);

}
