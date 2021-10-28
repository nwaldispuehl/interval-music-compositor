package ch.retorte.intervalmusiccompositor.spi.progress;

/**
 * Is able to react on an updated progress of a job.
 */
public interface ProgressListener {

  /**
   * Is called from the indicator with the current percentage of completion (0 (~0%) means the job is yet to be started, 100 (~100%) means that the job has been finished). Ideally this should be called every time the progress changed significantly.
   */
  default void onProgressUpdate(int percent) {}

  /**
   * Is called to provide a textual representation of the current state.
   */
  default void onProgressUpdate(String progress) {}
}
