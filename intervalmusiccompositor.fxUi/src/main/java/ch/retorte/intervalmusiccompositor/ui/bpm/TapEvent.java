package ch.retorte.intervalmusiccompositor.ui.bpm;

/**
 * The {@link TapEvent} holds the currently determined BPM (beats per minutes) value and if the value has converged.
 */
class TapEvent {

  private final int bpm;
  private final boolean converged;

  TapEvent(int bpm, boolean converged) {
    this.bpm = bpm;
    this.converged = converged;
  }

  int getBpm() {
    return bpm;
  }

  boolean isConverged() {
    return converged;
  }
}
