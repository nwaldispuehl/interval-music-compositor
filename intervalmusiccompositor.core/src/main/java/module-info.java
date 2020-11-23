module intervalmusiccompositor.core {
  exports ch.retorte.intervalmusiccompositor.audiofile;
  exports ch.retorte.intervalmusiccompositor.cache;
  exports ch.retorte.intervalmusiccompositor.util;
  exports ch.retorte.intervalmusiccompositor.compilation;
  exports ch.retorte.intervalmusiccompositor.output;
  exports ch.retorte.intervalmusiccompositor.player;

  requires intervalmusiccompositor.spi;
  requires intervalmusiccompositor.commons;
  requires intervalmusiccompositor.commons.audio;
  requires intervalmusiccompositor.build.dependencies;

  requires java.desktop;
}
