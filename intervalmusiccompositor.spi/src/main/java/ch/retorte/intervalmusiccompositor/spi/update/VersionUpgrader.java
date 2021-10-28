package ch.retorte.intervalmusiccompositor.spi.update;

import ch.retorte.intervalmusiccompositor.model.update.Version;
import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;

/**
 * Downloads a certain version of the software from the web repository.
 */
public interface VersionUpgrader {

    /**
     * Attempts to upgrade the software to the provided version.
     *
     * @param version the version we would like to have upgraded to.
     *
     * @throws RuntimeException if something fails.
     */
    void upgradeTo(Version version, ProgressListener progressListener);
}
