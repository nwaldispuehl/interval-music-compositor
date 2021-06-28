module intervalmusiccompositor.commons {
  exports ch.retorte.intervalmusiccompositor.commons;
  exports ch.retorte.intervalmusiccompositor.commons.platform;
  exports ch.retorte.intervalmusiccompositor.commons.preferences;

  requires intervalmusiccompositor.spi;

  requires java.desktop;
  requires jdk.management;
  requires java.prefs;

  // For testing
  requires hamcrest.core;
}
