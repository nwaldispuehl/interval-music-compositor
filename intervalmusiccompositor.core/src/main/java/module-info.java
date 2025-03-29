module intervalmusiccompositor.core {
    exports ch.retorte.intervalmusiccompositor.audiofile;
    exports ch.retorte.intervalmusiccompositor.cache;
    exports ch.retorte.intervalmusiccompositor.util;
    exports ch.retorte.intervalmusiccompositor.compilation;
    exports ch.retorte.intervalmusiccompositor.output;
    exports ch.retorte.intervalmusiccompositor.player;
    exports ch.retorte.intervalmusiccompositor.core.bundle;

    requires intervalmusiccompositor.spi;
    requires intervalmusiccompositor.commons;
    requires intervalmusiccompositor.commons.audio;

    requires java.desktop;
    requires org.tritonus.remaining;
    requires org.tritonus.dsp;

    // This is needed for SSL/TLS connectivity functionality when downloading the latest release archive with the updater.
    requires jdk.crypto.ec;
    requires jdk.crypto.cryptoki;
}
