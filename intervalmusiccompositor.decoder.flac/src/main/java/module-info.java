module intervalmusiccompositor.decoder.flac {
    exports ch.retorte.intervalmusiccompositor.decoder.flac;

    requires intervalmusiccompositor.spi;
    requires intervalmusiccompositor.commons;

    requires java.desktop;
    requires jaudiotagger;
    requires org.kc7bfi.jflac;
}
