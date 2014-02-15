package ch.retorte.intervalmusiccompositor.ui.bpm;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.AudioFileListType.BREAK;
import static java.lang.System.currentTimeMillis;

import java.text.DecimalFormat;
import java.util.List;

import ch.retorte.intervalmusiccompositor.commons.ArrayHelper;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.list.AudioFileListType;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import com.google.common.collect.Lists;

/**
 * @author nw
 */
public class DetermineBpmDialogControl {

  private MessageFormatBundle bundle = getBundle("ui_imc");

  private DetermineBpmDialog bpmDialog;

  private int tapWindowLength;
  private long tapBreakLength;
  private String unit;
  private List<Long> tapEventsTimestamps;
  private List<Double> bpmHistory;
  private int convergenceRange;
  private double convergenceThreshold;
  private DecimalFormat df = new DecimalFormat("###.##");

  private int audioFileIndex;
  private AudioFileListType audioFileListType;
  private MusicListControl musicListControl;
  private MessageProducer messageProducer;

  public DetermineBpmDialogControl(AudioFileListType audioFileListType, int audioFileIndex, MusicListControl musicListControl, MessageProducer messageProducer) {
    this.audioFileIndex = audioFileIndex;
    this.audioFileListType = audioFileListType;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;
  }

  public void startGui() {
    bpmDialog = new DetermineBpmDialog(this);

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        bpmDialog.setVisible(true);
      }
    });

    // Fill in various values
    bpmDialog.bpmField.setValue(getBpm());

    initializeTapDetermination();
  }

  private int getBpm() {
    if (audioFileListType == BREAK) {
      return musicListControl.getBreakBpm(audioFileIndex);
    }

    return musicListControl.getMusicBPM(audioFileIndex);
  }

  private void initializeTapDetermination() {
    tapWindowLength = Integer.valueOf(bundle.getString("imc.audio.determine_bpm.tap_window_length"));
    tapBreakLength = Long.valueOf(bundle.getString("imc.audio.determine_bpm.tap_break_length"));
    convergenceRange = Integer.valueOf(bundle.getString("imc.audio.bpm.convergence_range"));
    convergenceThreshold = Double.valueOf(bundle.getString("imc.audio.bpm.convergence_threshold"));
    unit = bundle.getString("ui.determine_bpm.tap_label.unit");

    resetLists();
  }

  public void updateBpm(int bpm) {
    if (audioFileListType == BREAK) {
      musicListControl.setBreakBpm(audioFileIndex, bpm);
    }
    else {
      musicListControl.setMusicBpm(audioFileIndex, bpm);
    }
  }

  public void startPlayingMusic() {
    if (audioFileListType == BREAK) {
      musicListControl.playBreakTrack(audioFileIndex);
    }
    else {
      musicListControl.playMusicTrack(audioFileIndex);
    }
  }

  public void stopPlayingMusic() {
    musicListControl.stopMusic();
  }

  public void tap() {

    int bpm = (Integer) bpmDialog.bpmField.getValue();
    double bpmReal = 0;

    // Check if the last event was too long ago and reset everything if yes
    if (tooMuchTimeHasPassed()) {
      resetLists();
    }

    // First add new event timestamp
    tapEventsTimestamps.add(currentTimeMillis());

    // Then get the average interval over the last n events
    long averageInterval = ArrayHelper.getAverageInterval(tapEventsTimestamps, tapWindowLength);


    // Calculate bpm
    if (averageInterval != 0) {
      bpmReal = 60 / (((double) averageInterval) / 1000);
      bpm = (int) Math.round(bpmReal);
    }

    bpmHistory.add(bpmReal);

    String color = "black";

    addDebugMessage("Tapped at: " + tapEventsTimestamps.get(tapEventsTimestamps.size()-1) + ", Calculated BPM: " + bpmReal);

    // Only set field if bpm value has converged
    if (converges()) {
      bpmDialog.bpmField.setValue(bpm);
      color = "blue";

      addDebugMessage("Tapping coverged");
    }

    bpmDialog.tapLabel.setText("<html><span style='color:" + color + ";'>" + df.format(bpmReal) + " " + unit + "</span></html>");
  }

  private void resetLists() {
    tapEventsTimestamps = Lists.newArrayList();
    bpmHistory = Lists.newArrayList();
  }

  private boolean converges() {
    return convergenceRange < tapEventsTimestamps.size() && ArrayHelper.isConvergent(bpmHistory, convergenceRange, convergenceThreshold);
  }

  private boolean tooMuchTimeHasPassed() {
    return 0 < tapEventsTimestamps.size() && tapBreakLength < (currentTimeMillis() - tapEventsTimestamps.get(tapEventsTimestamps.size() - 1));
  }

  public void closeDetermineBpmDialog() {

    // Tidy stuff
    stopPlayingMusic();

    bpmDialog.setVisible(false);
    bpmDialog.dispose();
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }


}
