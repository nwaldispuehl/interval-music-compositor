package ch.retorte.intervalmusiccompositor.soundeffect;

import java.io.InputStream;

/**
 * A sound effect references (usually relatively short) sound data and is played at a certain point in time in the interval cycle.
 */
public class SoundEffect {

  //---- Fields

  private String id;
  private String resource;
  private long durationMillis;


  //---- Constructor

  public SoundEffect(String id, String resource, long durationMillis) {
    this.id = id;
    this.resource = resource;
    this.durationMillis = durationMillis;
  }


  //---- Methods

  public String getId() {
    return id;
  }

  public InputStream getResourceStream() {
    return this.getClass().getResourceAsStream(resource);
  }

  public long getDurationMillis() {
    return durationMillis;
  }



}
