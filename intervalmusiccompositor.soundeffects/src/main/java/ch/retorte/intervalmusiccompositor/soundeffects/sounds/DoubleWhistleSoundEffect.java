package ch.retorte.intervalmusiccompositor.soundeffects.sounds;

import ch.retorte.intervalmusiccompositor.commons.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;

/**
 * Represents the double whistle sound effect.
 */
public class DoubleWhistleSoundEffect extends SoundEffect {

  //---- Static

  private final static String ID = "double_whistle";
  private final static String RESOURCE = "/double_whistle_1s.wav";
  private final static long DISPLAY_DURATION = 1000;


  //---- Constructor

  public DoubleWhistleSoundEffect() {
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisOf(DoubleWhistleSoundEffect.class.getResourceAsStream(RESOURCE)), DISPLAY_DURATION);
  }
}
