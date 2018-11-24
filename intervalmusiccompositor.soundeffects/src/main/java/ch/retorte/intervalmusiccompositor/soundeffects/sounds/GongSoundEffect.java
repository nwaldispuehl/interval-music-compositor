package ch.retorte.intervalmusiccompositor.soundeffects.sounds;

import ch.retorte.intervalmusiccompositor.commons.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;

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
    super(ID, RESOURCE, new AudioStreamUtil().lengthInMillisOf(GongSoundEffect.class.getResourceAsStream(RESOURCE)), DISPLAY_DURATION);
  }
}
