package ch.retorte.intervalmusiccompositor.soundeffect;

/**
 * Notes the point in time a {@link SoundEffect} happens.
 */
public class SoundEffectOccurrence {

  //---- Fields

  private SoundEffect soundEffect;
  private long timeMillis;


  //---- Constructor

  public SoundEffectOccurrence(SoundEffect soundEffect, long timeMillis) {
    this.soundEffect = soundEffect;
    this.timeMillis = timeMillis;
  }


  //---- Methods


  public SoundEffect getSoundEffect() {
    return soundEffect;
  }

  public long getTimeMillis() {
    return timeMillis;
  }
}
