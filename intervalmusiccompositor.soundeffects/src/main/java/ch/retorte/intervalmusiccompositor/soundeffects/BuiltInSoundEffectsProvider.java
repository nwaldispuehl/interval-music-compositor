package ch.retorte.intervalmusiccompositor.soundeffects;

import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.sounds.DoubleWhistleSoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.sounds.GongSoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffects.sounds.WhistleSoundEffect;
import ch.retorte.intervalmusiccompositor.spi.soundeffects.SoundEffectsProvider;

import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Offers a collection of already existing sound effects to choose from.
 */
public class BuiltInSoundEffectsProvider implements SoundEffectsProvider {

  //---- Fields

  private final GongSoundEffect gongSoundEffect = new GongSoundEffect();
  private final WhistleSoundEffect whistleSoundEffect = new WhistleSoundEffect();
  private final DoubleWhistleSoundEffect doubleWhistleSoundEffect = new DoubleWhistleSoundEffect();


  //---- Methods

  @Override
  public Collection<SoundEffect> getSoundEffects() {
    return asList(gongSoundEffect,
                  whistleSoundEffect,
                  doubleWhistleSoundEffect);
  }
}
