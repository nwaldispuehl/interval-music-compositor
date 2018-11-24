package ch.retorte.intervalmusiccompositor.model.list;

/**
 * Specifies how extracts are taken out of the tracks.
 * 
 * @author nw
 */
public enum EnumerationMode {

  /** From a track we take a random extract, then we advance to the next track. */
  SINGLE_EXTRACT,

  /** We take continuous extracts from a track as long as there is music left. If we're at the end, we advance to the next track. */
  CONTINUOUS
}
