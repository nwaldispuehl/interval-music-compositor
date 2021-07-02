package ch.retorte.intervalmusiccompositor.commons.bundle;

import ch.retorte.intervalmusiccompositor.commons.Utf8Bundle;

/**
 * Provides a bundle.
 */
public class BundleProvider {

    private final
    String bundleBaseName;

    protected BundleProvider(String bundleBaseName) {
        this.bundleBaseName = bundleBaseName;
    }

    public MessageFormatBundle getBundle() {
        return Utf8Bundle.getBundle(bundleBaseName);
    }

}
