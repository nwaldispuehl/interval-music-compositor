package ch.retorte.intervalmusiccompositor.soundeffects.sounds;

import ch.retorte.intervalmusiccompositor.commons.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;

/**
 * Represents the whistle sound effect.
 */
public class WhistleSoundEffect extends SoundEffect {

  //---- Static

  private final static String ID = "whistle";
  private final static String RESOURCE = "/whistle_1s.wav";
  private final static long DISPLAY_DURATION = 1000;


  //---- Constructor

  public WhistleSoundEffect() {
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisecondsOf(WhistleSoundEffect.class.getResourceAsStream(RESOURCE)), DISPLAY_DURATION);
  }
}
