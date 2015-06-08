package ch.retorte.intervalmusiccompositor.spi.progress;

/**
 * Indicates that the class supports progress updates.
 */
public interface ProgressUpdatable {

  /**
   * Sets a progress indicator which is notified on progress changes.
   */
  void setProgressListener(ProgressListener progressListener);
}
