module intervalmusiccompositor.encoder.mpeg {
  exports ch.retorte.intervalmusiccompositor.encoder.mp3;

  requires intervalmusiccompositor.spi;
  requires intervalmusiccompositor.commons;

  requires java.desktop;
  requires net.sourceforge.lame;
}
