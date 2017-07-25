package ch.retorte.intervalmusiccompositor.soundeffects;

import ch.retorte.intervalmusiccompositor.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;

public class GongSoundEffect extends SoundEffect {

  //---- Static

  private final static String ID = "gong";
  private final static String RESOURCE = "/gong_2s.wav";

  //---- Constructor

  public GongSoundEffect() {
    super(ID, new AudioStreamUtil().streamFromWaveResource(RESOURCE));
  }
}
