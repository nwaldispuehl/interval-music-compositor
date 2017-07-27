package ch.retorte.intervalmusiccompositor.compilation;

import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.arrayMerge16bit;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.extract;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.insertAt;
import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.ArrayHelper;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistItem;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffect;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * The {@link Compilation} collects all data needed for the final compilation, that is, the desired intervals, the files, etc, and offers means for generating
 * the compilation out of this data.
 * 
 * @author nw
 */
public class Compilation {

  private MessageFormatBundle bundle = getBundle("core_imc");

  private final SoundHelper soundHelper;
  private MessageProducer messageProducer;

  public Compilation(SoundHelper soundhelper, MessageProducer messageProducer) {
    this.soundHelper = soundhelper;
    this.messageProducer = messageProducer;
  }

  synchronized byte[] generateCompilation(Playlist playlist) throws IOException, UnsupportedAudioFileException {
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

    if (playlist.hasSoundEffects()) {
      for (SoundEffectOccurrence s : playlist.getSoundEffects()) {
        addDebugMessage("Adding sound effect " + s.getSoundEffect().getId() + " at " + s.getTimeMillis());
        addSoundEffect(result, s);
      }
    }

    return result;
  }

  private void addSoundEffect(byte[] compilation, SoundEffectOccurrence soundEffectOccurrence) throws IOException, UnsupportedAudioFileException {

    // TODO fade 0.2 s before and after sound effect

//    soundHelper.linearBlend(playlistItemByteArray, playlist.getBlendTime());

    int boundarySamples = soundHelper.getSamplesFromSeconds(0.2);

    byte[] soundEffect = audioFrom(soundEffectOccurrence.getSoundEffect());
    int soundEffectStartSamples = soundHelper.getSamplesFromSeconds(soundEffectOccurrence.getTimeMillis() / 1000);
    int soundEffectLengthSamples = soundHelper.getSamplesFromSeconds(soundEffectOccurrence.getSoundEffect().getDurationMillis() / 1000);
    int soundEffectEndSamples = soundEffectStartSamples + soundEffectLengthSamples;

    // Fade in
    byte[] beforeBoundary = extract(compilation, soundEffectStartSamples - boundarySamples, soundEffectStartSamples);
    byte[] fadeOutBeforeBoundary = soundHelper.fadeOut(beforeBoundary);
    insertAt(compilation, fadeOutBeforeBoundary, soundEffectStartSamples - boundarySamples);

    // Fade out (We only fade out if there is still compilation left)
    if (soundEffectEndSamples + boundarySamples <= compilation.length) {
      byte[] afterBoundary = extract(compilation, soundEffectEndSamples, soundEffectEndSamples + boundarySamples);
      byte[] fadeOutAfterBoundary = soundHelper.fadeOut(afterBoundary);
      insertAt(compilation, fadeOutAfterBoundary, soundEffectEndSamples);
    }

    arrayMerge16bit(soundEffect, 0, compilation, soundEffectStartSamples, soundEffect.length);
  }

  private byte[] audioFrom(SoundEffect soundEffect) throws IOException, UnsupportedAudioFileException {
    AudioInputStream audioInputStream = new AudioStreamUtil().audioInputStreamFrom(soundEffect.getResourceStream());
    return soundHelper.convert(audioInputStream);
  }

  private byte[] getByteArrayFrom(PlaylistItem playlistItem) throws IOException {
    if (playlistItem.isSilentBreak()) {
      return soundHelper.generateSilenceOfLength((playlistItem.getExtractDurationInSeconds()));
    }

    AudioInputStream leveledStream = soundHelper.getLeveledStream(playlistItem.getAudioFile().getAudioInputStream(), getVolumeRatioOf(playlistItem));
    return soundHelper.getStreamPart(leveledStream, playlistItem.getExtractStartInMilliseconds(), playlistItem.getExtractDurationInMilliseconds());
  }

  /**
   * We try to calculate the volume ratio of the extract we are going to insert into the compilation. This leads to a more homogeneous volume over the
   * compilation.
   */
  private float getVolumeRatioOf(PlaylistItem playlistItem) {
    int maximalAverageAmplitude = Integer.parseInt(bundle.getString("imc.audio.volume.max_average_amplitude"));
    int averageAmplitudeWindowSize = Integer.parseInt(bundle.getString("imc.audio.volume.window_size"));

    IAudioFile audioFile = playlistItem.getAudioFile();

    try {
    AudioInputStream streamExtract = soundHelper.getStreamExtract(audioFile.getAudioInputStream(), (int) playlistItem.getExtractStartInSeconds(),
        (int) playlistItem.getExtractDurationInSeconds());
      float averageAmplitude = soundHelper.getAvgAmplitude(streamExtract, averageAmplitudeWindowSize);
      return maximalAverageAmplitude / averageAmplitude;
    }
    catch (IOException e) {
      addDebugMessage("Problems when calculating amplitude: " + e.getMessage());
    }
    
    return audioFile.getVolumeRatio();
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
