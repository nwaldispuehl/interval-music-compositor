package ch.retorte.intervalmusiccompositor.model.soundeffect;

import java.io.InputStream;

/**
 * A sound effect references (usually relatively short) sound data and is played at a certain point in time in the interval cycle.
 */
public class SoundEffect {

  //---- Fields

  private String id;
  private String resource;
  private long effectiveDurationMillis;
  private long displayDurationMillis;


  //---- Constructor

  /**
   * Creates a new instance of a sound effect.
   *
   * @param id human readable identification string
   * @param resource reference to the 44100khz stereo WAV file holding the actual sound effect
   * @param effectiveDurationMillis the length of the sound effect
   * @param displayDurationMillis a 'nice' length value to be used for display purposes. If the actual length is 1920 ms, this could be set to 2000.
   */
  public SoundEffect(String id, String resource, long effectiveDurationMillis, long displayDurationMillis) {
    this.id = id;
    this.resource = resource;
    this.effectiveDurationMillis = effectiveDurationMillis;
    this.displayDurationMillis = displayDurationMillis;
  }


  //---- Methods

  public String getId() {
    return id;
  }

  public InputStream getResourceStream() {
    return this.getClass().getResourceAsStream(resource);
  }

  public long getDurationMillis() {
    return effectiveDurationMillis;
  }

  public long getDisplayDurationMillis() {
    return displayDurationMillis;
  }

  @Override
  public String toString() {
    return id + " (" + displayDurationMillis + ")";
  }
}
