package ch.retorte.intervalmusiccompositor.soundeffect;

import javax.sound.sampled.AudioInputStream;

/**
 * A sound effect references sound data and is played at a certain point in time in the interval cycle.
 */
public class SoundEffect {

  //---- Fields

  private String title;
  private AudioInputStream data;

  //---- Constructor

  public SoundEffect(String title, AudioInputStream data) {
    this.title = title;
    this.data = data;
  }

  //---- Methods


  public String getTitle() {
    return title;
  }

  public AudioInputStream getData() {
    return data;
  }
}
