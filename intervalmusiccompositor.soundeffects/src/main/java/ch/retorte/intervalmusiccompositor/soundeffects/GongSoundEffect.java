package ch.retorte.intervalmusiccompositor.soundeffects;

import ch.retorte.intervalmusiccompositor.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;

/**
 * Represents the gong sound effect.
 */
public class GongSoundEffect extends SoundEffect {

  //---- Static

  private final static String ID = "gong";
  private final static String RESOURCE = "/gong_2s.wav";
  private final static long DISPLAY_DURATION = 2000;


  //---- Constructor

  public GongSoundEffect() {
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisOf(RESOURCE), DISPLAY_DURATION);
  }
}
