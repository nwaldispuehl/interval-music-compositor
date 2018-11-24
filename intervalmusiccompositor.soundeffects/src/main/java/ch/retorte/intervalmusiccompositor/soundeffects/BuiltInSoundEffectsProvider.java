package ch.retorte.intervalmusiccompositor.soundeffects;

import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.sounds.WhistleSoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.sounds.DoubleWhistleSoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.sounds.GongSoundEffect;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * Offers a collection of already existing sound effects to choose from.
 */
public class BuiltInSoundEffectsProvider implements SoundEffectsProvider {

  //---- Fields

  private GongSoundEffect gongSoundEffect = new GongSoundEffect();
  private WhistleSoundEffect whistleSoundEffect = new WhistleSoundEffect();
  private DoubleWhistleSoundEffect doubleWhistleSoundEffect = new DoubleWhistleSoundEffect();


  //---- Methods

  @Override
  public Collection<SoundEffect> getSoundEffects() {
    return Lists.newArrayList(gongSoundEffect,
                              whistleSoundEffect,
                              doubleWhistleSoundEffect);
  }
}
