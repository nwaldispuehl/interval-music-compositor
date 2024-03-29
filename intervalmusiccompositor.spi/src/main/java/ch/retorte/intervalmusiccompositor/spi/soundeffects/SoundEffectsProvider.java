package ch.retorte.intervalmusiccompositor.spi.soundeffects;

import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffect;

import java.util.Collection;

/**
 * Provides a number of {@link SoundEffect}s.
 */
public interface SoundEffectsProvider {

  Collection<SoundEffect> getSoundEffects();
}
