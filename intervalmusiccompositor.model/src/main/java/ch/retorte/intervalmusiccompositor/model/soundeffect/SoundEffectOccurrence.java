package ch.retorte.intervalmusiccompositor.model.soundeffect;

/**
 * Notes the point in time a {@link SoundEffect} happens.
 */
public class SoundEffectOccurrence {

  //---- Fields

  private SoundEffect soundEffect;
  private long startTimeMs;


  //---- Constructor

  /**
   * Creates a new sound effect occurrence.
   *
   * @param soundEffect the {@link SoundEffect} to be played.
   * @param startTimeMs the time in milliseconds it should start playing.
   */
  public SoundEffectOccurrence(SoundEffect soundEffect, long startTimeMs) {
    this.soundEffect = soundEffect;
    this.startTimeMs = startTimeMs;
  }


  //---- Methods

  public SoundEffect getSoundEffect() {
    return soundEffect;
  }

  public void setSoundEffect(SoundEffect soundEffect) {
    this.soundEffect = soundEffect;
  }

  public long getStartTimeMs() {
    return startTimeMs;
  }

  public void setStartTimeMs(long startTimeMs) {
    this.startTimeMs = startTimeMs;
  }
}
