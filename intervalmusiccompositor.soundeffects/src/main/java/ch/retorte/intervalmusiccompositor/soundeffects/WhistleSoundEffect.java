package ch.retorte.intervalmusiccompositor.soundeffects;

import ch.retorte.intervalmusiccompositor.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;

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
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisOf(RESOURCE), DISPLAY_DURATION);
  }
}
