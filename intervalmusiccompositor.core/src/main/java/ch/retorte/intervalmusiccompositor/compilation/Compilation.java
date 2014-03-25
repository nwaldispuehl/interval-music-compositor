package ch.retorte.intervalmusiccompositor.compilation;

import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.arrayMerge16bit;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistItem;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * The {@link Compilation} collects all data needed for the final compilation, that is, the desired intervals, the files, etc, and offers means for generating
 * the compilation out of this data.
 * 
 * @author nw
 */
public class Compilation {

  private final SoundHelper soundHelper;
  private MessageProducer messageProducer;

  public Compilation(SoundHelper soundhelper, MessageProducer messageProducer) {
    this.soundHelper = soundhelper;
    this.messageProducer = messageProducer;
  }

  public synchronized byte[] generateCompilation(Playlist playlist) throws IOException {
    byte[] result = new byte[soundHelper.getSamplesFromSeconds(playlist.getTotalLengthInSeconds())];

    addDebugMessage("Generate compilation of length: " + playlist.getTotalLengthInSeconds() + " s");

    int currentPosition = 0;
    for (PlaylistItem playlistItem : playlist) {

      byte[] playlistItemByteArray = getByteArrayFrom(playlistItem);
      byte[] blendedPlaylistItemByteArray = soundHelper.linearBlend(playlistItemByteArray, playlist.getBlendTime());

      arrayMerge16bit(blendedPlaylistItemByteArray, 0, result, currentPosition, blendedPlaylistItemByteArray.length);
      addDebugMessage("Adding to compilation at " + soundHelper.getSecondsFromSamples(currentPosition) + ": " + playlistItem);

      currentPosition += blendedPlaylistItemByteArray.length;

      if (playlist.getBlendMode() == CROSS) {
        currentPosition -= soundHelper.getSamplesFromSeconds(0.5 * playlist.getBlendTime());
      }

    }

    return result;
  }

  private byte[] getByteArrayFrom(PlaylistItem playlistItem) throws IOException {
    if (playlistItem.isSilentBreak()) {
      return soundHelper.generateSilenceOfLength((playlistItem.getExtractDuration() / 1000));
    }

    IAudioFile audioFile = playlistItem.getAudioFile();

    AudioInputStream ais = audioFile.getAudioInputStream();
    AmplitudeAudioInputStream aais = new AmplitudeAudioInputStream(ais);
    aais.setAmplitudeLinear(audioFile.getVolumeRatio());

    return soundHelper.getStreamPart(aais, playlistItem.getExtractStartInMilliseconds(), playlistItem.getExtractDuration());
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
