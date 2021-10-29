module intervalmusiccompositor.decoder.mpeg {
    exports ch.retorte.intervalmusiccompositor.decoder.mp3;

    requires intervalmusiccompositor.spi;
    requires intervalmusiccompositor.commons;

    requires java.desktop;
    requires jaudiotagger;
    requires javazoom.javalayer;
    requires javazoom.spi.mp3spi;
}
