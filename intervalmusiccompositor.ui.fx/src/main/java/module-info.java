module intervalmusiccompositor.ui.fx {
  exports ch.retorte.intervalmusiccompositor.ui;
  exports ch.retorte.intervalmusiccompositor.ui.preferences;

  exports ch.retorte.intervalmusiccompositor.ui.mainscreen to javafx.fxml;
  exports ch.retorte.intervalmusiccompositor.ui.audiofilelist to javafx.fxml;
  exports ch.retorte.intervalmusiccompositor.ui.debuglog to javafx.fxml;

  opens ch.retorte.intervalmusiccompositor.ui.mainscreen to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.soundeffects to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.debuglog to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.firststart to javafx.fxml;

  opens fonts;
  opens images;
  opens layouts;
  opens styles;

  requires intervalmusiccompositor.spi;
  requires intervalmusiccompositor.commons;

  requires java.desktop;
  requires com.google.common;
  requires org.apache.commons.lang3;

  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.swing;
}
