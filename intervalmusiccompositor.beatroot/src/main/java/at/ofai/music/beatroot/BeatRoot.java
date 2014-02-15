package at.ofai.music.beatroot;

import javax.sound.sampled.AudioInputStream;

import ch.retorte.intervalmusiccompositor.spi.bpm.BPMCalculator;
import at.ofai.music.util.EventList;

/**
 * Custom front end for the beat root engine; takes a sound sample in the form
 * of an {@link AudioInputStream} and determines the most plausible beat per
 * minute (BPM) value.
 */
public class BeatRoot implements BPMCalculator {

  @Override
  public Integer calculateBPM(AudioInputStream audioInputStream) {

    AudioProcessor ap = new AudioProcessor();

    ap.setInputStream(audioInputStream);
    ap.processFile();

    EventList beats = AudioProcessor.beatTrack(ap.onsetList, null);
    ap.closeStreams();

    // Such empty list means that there was no candidate found.
    if (beats.size() == 0) {
      return null;
    }

    return (int) Math.round(60 / AudioProcessor.getMedianIBI(beats.toOnsetArray()));
  }

}
