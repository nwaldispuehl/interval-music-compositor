package ch.retorte.intervalmusiccompositor.spi.update;

import ch.retorte.intervalmusiccompositor.model.update.Version;

/**
 * Interface for facilities which check for a software update.
 * 
 * @author nw
 */
public interface UpdateAvailabilityChecker {

  /**
   * Determines if an update is available.
   * 
   * @return true if an update is available, false otherwise.
   */
  boolean isUpdateAvailable();

  /**
   * Returns the latest available version, if possible.
   * 
   * @return the latest available version, or null if none was found.
   */
  Version getLatestVersion();
}
