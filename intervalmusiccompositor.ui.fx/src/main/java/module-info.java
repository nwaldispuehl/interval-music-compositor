module intervalmusiccompositor.ui.fx {
  exports ch.retorte.intervalmusiccompositor.ui;
  exports ch.retorte.intervalmusiccompositor.ui.preferences;

  exports ch.retorte.intervalmusiccompositor.ui.audiofilelist to javafx.fxml;
  exports ch.retorte.intervalmusiccompositor.ui.mainscreen to javafx.fxml;
  exports ch.retorte.intervalmusiccompositor.ui.debuglog to javafx.fxml;

  opens ch.retorte.intervalmusiccompositor.ui to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.audiofilelist to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.bpm to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.bundle to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.debuglog to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.firststart to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.graphics to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.mainscreen to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.preferences to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.soundeffects to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.updatecheck to javafx.fxml;
  opens ch.retorte.intervalmusiccompositor.ui.utils to javafx.fxml;

  opens fonts;
  opens images;
  opens images.tracklist;
  opens layouts;
  opens styles;

  requires intervalmusiccompositor.model;
  requires intervalmusiccompositor.spi;
  requires intervalmusiccompositor.commons;
  requires intervalmusiccompositor.core;

  requires java.desktop;
  requires org.apache.commons.lang3;

  requires javafx.base;
  requires javafx.graphics;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.swing;
}
