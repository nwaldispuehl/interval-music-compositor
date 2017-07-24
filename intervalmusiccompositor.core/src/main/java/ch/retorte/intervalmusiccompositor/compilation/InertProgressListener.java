package ch.retorte.intervalmusiccompositor.compilation;

import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;

/**
 * This inert progress listener fires only if the value changes compared to the last invocation.
 */
abstract class InertProgressListener implements ProgressListener {

  private int oldPercent;

  @Override
  public final void onProgressUpdate(int percent) {
    if (oldPercent != percent) {
      onProgressChange(percent);
      oldPercent = percent;
    }
  }

  protected abstract void onProgressChange(int percent);
}
