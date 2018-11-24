module intervalmusiccompositor.decoder.flac {
  exports ch.retorte.intervalmusiccompositor.decoder.flac;

  requires intervalmusiccompositor.spi;

  requires java.desktop;
  requires org.kc7bfi.jflac;
  requires jaudiotagger;
  requires com.google.common;
}
