package ch.retorte.intervalmusiccompositor.ui.bpm;

import ch.retorte.intervalmusiccompositor.commons.ArrayHelper;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * Tracks tapping events and calculates BPM values.
 */
public class Tap {

  private final List<Long> tapEventsTimestamps = new ArrayList<>();
  private final List<Double> bpmHistory = new ArrayList<>();

  private final MessageProducer messageProducer;
  private final MessageFormatBundle bundle;
  private int tapWindowLength;
  private long tapBreakLength;
  private int convergenceRange;
  private double convergenceThreshold;

  public Tap(MessageProducer messageProducer, MessageFormatBundle bundle) {
    this.messageProducer = messageProducer;
    this.bundle = bundle;

    initializeConstants();
  }

  private void initializeConstants() {
    tapWindowLength = Integer.parseInt(bundle.getString("imc.audio.determine_bpm.tap_window_length"));
    tapBreakLength = Long.parseLong(bundle.getString("imc.audio.determine_bpm.tap_break_length"));
    convergenceRange = Integer.parseInt(bundle.getString("imc.audio.bpm.convergence_range"));
    convergenceThreshold = Double.parseDouble(bundle.getString("imc.audio.bpm.convergence_threshold"));
  }

  TapEvent tap() {
    boolean converges = false;
    double bpmReal = 0;

    if (tooMuchTimeHasPassed()) {
      clearTapHistory();
    }

    addEventTimestamp();

    long averageInterval = ArrayHelper.getAverageInterval(tapEventsTimestamps, tapWindowLength);
    if (averageInterval != 0) {
      bpmReal = 60 / (((double) averageInterval) / 1000);
    }

    bpmHistory.add(bpmReal);

    addDebugMessage("Tapped at: " + tapEventsTimestamps.get(tapEventsTimestamps.size()-1) + ", Calculated BPM: " + bpmReal);

    if (converges()) {
      converges = true;
      addDebugMessage("Tapping converged");
    }

    return new TapEvent(round(bpmReal), converges);
  }

  private void addEventTimestamp() {
    tapEventsTimestamps.add(currentTimeMillis());
  }

  private int round(double value) {
    return (int) Math.round(value);
  }

  private boolean tooMuchTimeHasPassed() {
    return 0 < tapEventsTimestamps.size() && tapBreakLength < (currentTimeMillis() - tapEventsTimestamps.get(tapEventsTimestamps.size() - 1));
  }

  private void clearTapHistory() {
    tapEventsTimestamps.clear();
    bpmHistory.clear();
  }

  private boolean converges() {
    return convergenceRange < tapEventsTimestamps.size() && ArrayHelper.isConvergent(bpmHistory, convergenceRange, convergenceThreshold);
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
