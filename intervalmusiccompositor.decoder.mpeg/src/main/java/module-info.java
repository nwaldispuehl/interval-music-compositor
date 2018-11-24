module intervalmusiccompositor.decoder.mpeg {
  exports ch.retorte.intervalmusiccompositor.decoder.mp3;

  requires intervalmusiccompositor.spi;

  requires java.desktop;
  requires javazoom.spi.mp3spi;
  requires javazoom.javalayer;
  requires com.google.common;
}
