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

    // For testing
    requires hamcrest.core;
}
