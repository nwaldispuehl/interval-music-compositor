module intervalmusiccompositor.commons {
    exports ch.retorte.intervalmusiccompositor.commons;
    exports ch.retorte.intervalmusiccompositor.commons.platform;
    exports ch.retorte.intervalmusiccompositor.commons.preferences;
    exports ch.retorte.intervalmusiccompositor.commons.bundle;

    requires intervalmusiccompositor.spi;
    requires intervalmusiccompositor.model;

    requires java.desktop;
    requires jdk.management;
    requires java.prefs;
}
