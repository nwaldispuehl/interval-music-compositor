package ch.retorte.intervalmusiccompositor;

import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.GongSoundEffect;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * Offers a collection of already existing sound effects to choose.
 */
public class BuiltInSoundEffectsProvider implements SoundEffectsProvider {

  //---- Fields

  private GongSoundEffect gongSoundEffect = new GongSoundEffect();


  //---- Methods

  @Override
  public Collection<SoundEffect> getSoundEffects() {
    return Lists.newArrayList(gongSoundEffect);
  }
}
