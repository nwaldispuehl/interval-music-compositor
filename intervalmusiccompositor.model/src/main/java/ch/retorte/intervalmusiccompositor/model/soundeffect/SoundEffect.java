package ch.retorte.intervalmusiccompositor.model.soundeffect;

import java.io.InputStream;

/**
 * A sound effect references (usually relatively short) sound data and is played at a certain point in time in the interval cycle.
 */
public class SoundEffect {

  //---- Fields

  private final String id;
  private final String resource;
  private final double effectiveDurationMs;
  private final long displayDurationMs;


  //---- Constructor

  /**
   * Creates a new instance of a sound effect.
   *
   * @param id human-readable identification string
   * @param resource reference to the 44100khz stereo WAV file holding the actual sound effect
   * @param effectiveDurationMs the length of the sound effect in ms, but as double value for increased precision.
   * @param displayDurationMs a 'nice' length value to be used for display purposes. If the actual length is 1920 ms, this could be set to 2000.
   */
  public SoundEffect(String id, String resource, double effectiveDurationMs, long displayDurationMs) {
    this.id = id;
    this.resource = resource;
    this.effectiveDurationMs = effectiveDurationMs;
    this.displayDurationMs = displayDurationMs;
  }


  //---- Methods

  public String getId() {
    return id;
  }

  public InputStream getResourceStream() {
    return this.getClass().getResourceAsStream(resource);
  }

  public double getDurationMs() {
    return effectiveDurationMs;
  }

  public long getDisplayDurationMs() {
    return displayDurationMs;
  }

  @Override
  public String toString() {
    return id + " (" + displayDurationMs + ")";
  }
}
