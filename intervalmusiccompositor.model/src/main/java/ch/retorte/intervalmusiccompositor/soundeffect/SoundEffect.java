package ch.retorte.intervalmusiccompositor.soundeffect;

import javax.sound.sampled.AudioInputStream;

/**
 * A sound effect references sound data and is played at a certain point in time in the interval cycle.
 */
public class SoundEffect {

  //---- Fields

  private String id;
  private AudioInputStream data;
  private long durationMillis;


  //---- Constructor

  public SoundEffect(String id, AudioInputStream data) {
    this.id = id;
    this.data = data;
    this.durationMillis = getStreamLengthInMilliSecondsOf(data);
  }


  //---- Methods

  public String getId() {
    return id;
  }

  public AudioInputStream getData() {
    return data;
  }

  public long getDurationMillis() {
    return durationMillis;
  }

  private long getStreamLengthInMilliSecondsOf(AudioInputStream inputStream) {
    return (long) ((inputStream.getFrameLength() / inputStream.getFormat().getFrameRate()) * 1000);
  }

}
