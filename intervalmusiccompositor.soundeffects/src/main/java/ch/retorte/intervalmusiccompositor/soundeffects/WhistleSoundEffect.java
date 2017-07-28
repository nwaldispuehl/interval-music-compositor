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

  //---- Constructor

  public WhistleSoundEffect() {
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisOf(RESOURCE));
  }
}
