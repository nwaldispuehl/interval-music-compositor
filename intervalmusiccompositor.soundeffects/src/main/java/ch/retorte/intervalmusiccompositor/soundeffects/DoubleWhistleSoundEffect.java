package ch.retorte.intervalmusiccompositor.soundeffects;

import ch.retorte.intervalmusiccompositor.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;

/**
 * Represents the double whistle sound effect.
 */
public class DoubleWhistleSoundEffect extends SoundEffect {

  //---- Static

  private final static String ID = "double_whistle";
  private final static String RESOURCE = "/double_whistle_1s.wav";

  //---- Constructor

  public DoubleWhistleSoundEffect() {
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisOf(RESOURCE));
  }
}
