module intervalmusiccompositor.spi {
  exports ch.retorte.intervalmusiccompositor.spi;
  exports ch.retorte.intervalmusiccompositor.spi.audio;
  exports ch.retorte.intervalmusiccompositor.spi.decoder;
  exports ch.retorte.intervalmusiccompositor.spi.encoder;
  exports ch.retorte.intervalmusiccompositor.spi.bpm;
  exports ch.retorte.intervalmusiccompositor.spi.messagebus;
  exports ch.retorte.intervalmusiccompositor.spi.progress;
  exports ch.retorte.intervalmusiccompositor.spi.soundeffects;
  exports ch.retorte.intervalmusiccompositor.spi.update;

  requires transitive intervalmusiccompositor.model;

  requires java.desktop;
}
