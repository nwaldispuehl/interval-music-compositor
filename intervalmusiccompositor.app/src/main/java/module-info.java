module intervalmusiccompositor.app {
    requires intervalmusiccompositor.spi;

    requires intervalmusiccompositor.commons;
    requires intervalmusiccompositor.commons.audio;

    requires intervalmusiccompositor.core;
    requires intervalmusiccompositor.fxUi;

    requires intervalmusiccompositor.soundeffects;

    requires intervalmusiccompositor.beatroot;

    requires intervalmusiccompositor.decoder.wave;
    requires intervalmusiccompositor.decoder.flac;
    requires intervalmusiccompositor.decoder.mpeg;
    requires intervalmusiccompositor.decoder.ogg;
    requires intervalmusiccompositor.decoder.aac;

    requires intervalmusiccompositor.encoder.wave;
    requires intervalmusiccompositor.encoder.mpeg;
    requires intervalmusiccompositor.encoder.ogg;

    requires java.logging;

    requires com.google.common;

    opens ch.retorte.intervalmusiccompositor;
}
