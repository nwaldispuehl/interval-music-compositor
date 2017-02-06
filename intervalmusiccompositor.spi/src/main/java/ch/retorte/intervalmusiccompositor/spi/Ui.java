package ch.retorte.intervalmusiccompositor.spi;

import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.net.URI;

/**
 * @author nw
 */
public interface Ui {

  void quit();

  void launch();

  void setActive();

  void setInactive();

  void setEnvelopeImage(WritableImage envelopeImage);

  void openInDesktopBrowser(String url);
}
