package ch.retorte.intervalmusiccompositor.compilation;

import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.arrayMerge16bit;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.extract;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.insertAt;
import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.audio.AudioStreamUtil;
import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.playlist.Playlist;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistItem;
import ch.retorte.intervalmusiccompositor.playlist.PlaylistItemFragment;
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

  //---- Static

  private static final double SOUND_EFFECT_BOUNDARY_FADE_DURATION_S = 0.2;

  //---- Fields

  private MessageFormatBundle bundle = getBundle("core_imc");

  private final SoundHelper soundHelper;
  private MessageProducer messageProducer;

  //---- Constructor

  public Compilation(SoundHelper soundhelper, MessageProducer messageProducer) {
    this.soundHelper = soundhelper;
    this.messageProducer = messageProducer;
  }

  //---- Methods

  synchronized void generateCompilation(Playlist playlist, OutputStream outputStream) throws IOException, UnsupportedAudioFileException {
    addDebugMessage("Generate compilation of length: " + playlist.getTotalLengthInSeconds() + " s");

    // Before the item iteration: prefix with blend artifact if needed.
    if (playlist.isCrossFadingMode()) {
      Optional<PlaylistItem> first = playlist.getFirst();
      if (first.isPresent()) {
        byte[] blendIntroData = getByteArrayFrom(first.get().getMusicFragment());
        long startTimeMs = 0;
        long durationMs = playlist.getHalfBlendTimeMs();

        byte[] fadedBlendIntroDataExtract = getFadedExtract(blendIntroData, startTimeMs, durationMs, 0, 0.5);

        outputStream.write(fadedBlendIntroDataExtract);
      }
    }


    long timeSoFarMs = 0;

    if (playlist.isCrossFadingMode()) {
      timeSoFarMs += playlist.getHalfBlendTimeMs();
    }

    for (PlaylistItem playlistItem : playlist) {

      // Music Track
      byte[] playListItemData = getPlaylistItemDataFrom(playlist, playlistItem, timeSoFarMs);
      outputStream.write(playListItemData);

      timeSoFarMs += playlistItem.getStrictItemLengthMs();
    }


    // After the item iteration: Append blend artifact
    if (playlist.isCrossFadingMode()) {
      Optional<PlaylistItem> last = playlist.getLast();
      if (last.isPresent()) {
        PlaylistItemFragment lastFragment = last.get().hasBreakFragment() ? last.get().getBreakFragment() : last.get().getMusicFragment();

        byte[] blendOutroData = getByteArrayFrom(lastFragment);
        long durationMs = playlist.getHalfBlendTimeMs();
        long startTimeMs = lastFragment.getExtractDurationInMilliseconds() - durationMs;

        byte[] fadedBlendOutroDataExtract = getFadedExtract(blendOutroData, startTimeMs, durationMs, 0.5, 0);

        outputStream.write(fadedBlendOutroDataExtract);
      }
    }
  }

  private byte[] getPlaylistItemDataFrom(Playlist playlist, PlaylistItem playlistItem, long timeSoFarMs) throws IOException, UnsupportedAudioFileException {
    byte[] playlistItemData = new byte[soundHelper.getSamplesFromSeconds(playlistItem.getStrictItemLengthMs() / 1000)];

    PlaylistItemFragment musicFragment = playlistItem.getMusicFragment();

    double musicExtractDurationS = musicFragment.getExtractDurationInSeconds();
    if (playlist.isCrossFadingMode()) {
      musicExtractDurationS -= playlist.getBlendTimeS();
    }
    int musicSamples = soundHelper.getSamplesFromSeconds(musicExtractDurationS);

    addDebugMessage("Processing next playlist item starting at " + timeSoFarMs + " ms");

    // Any fade out from the previous track

    if (playlist.isCrossFadingMode()) {
      byte[] previousItemFadeOut = getPreviousItemFadeOutBytesFrom(playlist, playlistItem);
      arrayMerge16bit(previousItemFadeOut, 0, playlistItemData, 0, previousItemFadeOut.length);
    }

    // The music part

    addDebugMessage("Adding music track '" + musicFragment + "' at " + timeSoFarMs + " ms");
    byte[] musicData = getMusicBytesFrom(playlist, musicFragment);
    arrayMerge16bit(musicData, 0, playlistItemData, 0, musicData.length);

    // The break part

    if (playlistItem.hasBreakFragment()) {
      PlaylistItemFragment breakFragment = playlistItem.getBreakFragment();

      // Cross fading overlap

      if (playlist.isCrossFadingMode()) {
        // We're adding the parts lying in the previous/next track.
        byte[] fadeInBytes = getFadeInBytesFrom(playlist, breakFragment);
        arrayMerge16bit(fadeInBytes, 0, playlistItemData, musicSamples - fadeInBytes.length, fadeInBytes.length);

        byte[] fadeOutBytes = getFadeOutBytesFrom(playlist, musicFragment);
        arrayMerge16bit(fadeOutBytes, 0, playlistItemData, musicSamples, fadeOutBytes.length);
      }

      addDebugMessage("Adding break track '" + breakFragment + "' at " + (timeSoFarMs + (int)(musicExtractDurationS * 1000)) + " ms");
      byte[] breakData = getMusicBytesFrom(playlist, breakFragment);
      arrayMerge16bit(breakData, 0, playlistItemData, musicSamples, breakData.length);
    }

    // Any fade in of the next track

    if (playlist.isCrossFadingMode()) {
      byte[] nextItemFadeIn = getNextItemFadeInBytesFrom(playlist, playlistItem);
      arrayMerge16bit(nextItemFadeIn, 0, playlistItemData, playlistItemData.length - nextItemFadeIn.length, nextItemFadeIn.length);
    }

    for (SoundEffectOccurrence effect : playlistItem.getSoundEffects()) {
      addDebugMessage("Adding sound effect " + effect.getSoundEffect().getId() + " at " + (timeSoFarMs + effect.getTimeMillis()) + " ms");
      addSoundEffectTo(playlistItemData, effect);
    }

    return playlistItemData;
  }


  private byte[] getNextItemFadeInBytesFrom(Playlist playlist, PlaylistItem playlistItem) throws IOException {
    if (playlistItem.hasNext()) {
      return getFadeInBytesFrom(playlist, playlistItem.getNext().getMusicFragment());
    }
    else {
      return new byte[0];
    }
  }

  private byte[] getFadeInBytesFrom(Playlist playlist, PlaylistItemFragment fragment) throws IOException {
    byte[] musicData = getByteArrayFrom(fragment);
    long durationMs = playlist.getHalfBlendTimeMs();
    return getFadedExtract(musicData, 0, durationMs, 0, 0.5);
  }

  private byte[] getPreviousItemFadeOutBytesFrom(Playlist playlist,PlaylistItem playlistItem) throws IOException {
    if (playlistItem.hasPrevious()) {
      PlaylistItem previousItem = playlistItem.getPrevious();
      PlaylistItemFragment previousFragment = previousItem.hasBreakFragment() ? previousItem.getBreakFragment() : previousItem.getMusicFragment();
      return getFadeOutBytesFrom(playlist, previousFragment);
    }
    else {
      return new byte[0];
    }
  }

  private byte[] getFadeOutBytesFrom(Playlist playlist, PlaylistItemFragment fragment) throws IOException {
    byte[] musicData = getByteArrayFrom(fragment);
    long durationMs = playlist.getHalfBlendTimeMs();
    return getFadedExtract(musicData, fragment.getExtractDurationInMilliseconds() - durationMs, durationMs, 0.5, 0);
  }

  private byte[] getFadedExtract(byte[] input, long startMs, long durationMs, double startFactor, double endFactor) {
    byte[] inputExtract = soundHelper.getExtract(input, startMs, durationMs);
    return soundHelper.fade(inputExtract, startFactor, endFactor);
  }


  private byte[] getMusicBytesFrom(Playlist playlist, PlaylistItemFragment fragment) throws IOException {
    byte[] musicData = getByteArrayFrom(fragment);

    if (playlist.isCrossFadingMode()) {
      // Cut at front and end
      // blend 0.5 to 1 and vice versa.
      byte[] extract = soundHelper.getExtract(musicData, playlist.getHalfBlendTimeMs(), fragment.getExtractDurationInMilliseconds() - playlist.getBlendTimeMs());
      return soundHelper.doubleSidedLinearBlend(extract, playlist.getHalfBlendTimeS(), 0.5, 1);
    }
    else {
      return soundHelper.doubleSidedLinearBlend(musicData, playlist.getBlendTimeS(), 0, 1);
    }
  }

  private void addSoundEffectTo(byte[] playlistItemData, SoundEffectOccurrence soundEffectOccurrence) throws IOException, UnsupportedAudioFileException {
    int boundarySamples = soundHelper.getSamplesFromSeconds(SOUND_EFFECT_BOUNDARY_FADE_DURATION_S);

    byte[] soundEffect = audioFrom(soundEffectOccurrence.getSoundEffect());
    int soundEffectStartSamples = soundHelper.getSamplesFromSeconds(soundEffectOccurrence.getTimeMillis() / 1000);
    int soundEffectLengthSamples = soundHelper.getSamplesFromSeconds(soundEffectOccurrence.getSoundEffect().getDurationMillis() / 1000);
    int soundEffectEndSamples = soundEffectStartSamples + soundEffectLengthSamples;

    // Fade in (We only fade in if there is data before)
    if (0 <= soundEffectStartSamples - boundarySamples) {
      byte[] beforeBoundary = extract(playlistItemData, soundEffectStartSamples - boundarySamples, soundEffectStartSamples);
      byte[] fadeOutBeforeBoundary = soundHelper.fadeOut(beforeBoundary);
      insertAt(playlistItemData, fadeOutBeforeBoundary, soundEffectStartSamples - boundarySamples);
    }

    // Fade out (We only fade out if there is still compilation left)
    if (soundEffectEndSamples + boundarySamples <= playlistItemData.length) {
      byte[] afterBoundary = extract(playlistItemData, soundEffectEndSamples, soundEffectEndSamples + boundarySamples);
      byte[] fadeOutAfterBoundary = soundHelper.fadeIn(afterBoundary);
      insertAt(playlistItemData, fadeOutAfterBoundary, soundEffectEndSamples);
    }

    // Plug in sound effect
    insertAt(playlistItemData, soundEffect, soundEffectStartSamples);
  }

  private byte[] audioFrom(SoundEffect soundEffect) throws IOException, UnsupportedAudioFileException {
    AudioInputStream audioInputStream = new AudioStreamUtil().audioInputStreamFrom(soundEffect.getResourceStream());
    return soundHelper.convert(audioInputStream);
  }

  private byte[] getByteArrayFrom(PlaylistItemFragment playlistItemFragment) throws IOException {
    if (playlistItemFragment.isSilentBreak()) {
      return soundHelper.generateSilenceOfLength((playlistItemFragment.getExtractDurationInSeconds()));
    }

    AudioInputStream leveledStream = soundHelper.getLeveledStream(playlistItemFragment.getAudioFile().getAudioInputStream(), getVolumeRatioOf(playlistItemFragment));
    return soundHelper.getStreamPart(leveledStream, playlistItemFragment.getExtractStartInMilliseconds(), playlistItemFragment.getExtractDurationInMilliseconds());
  }

  /**
   * We try to calculate the volume ratio of the extract we are going to insert into the compilation. This leads to a more homogeneous volume over the
   * compilation.
   */
  private float getVolumeRatioOf(PlaylistItemFragment playlistItemFragment) {
    int maximalAverageAmplitude = Integer.parseInt(bundle.getString("imc.audio.volume.max_average_amplitude"));
    int averageAmplitudeWindowSize = Integer.parseInt(bundle.getString("imc.audio.volume.window_size"));

    IAudioFile audioFile = playlistItemFragment.getAudioFile();

    try {
    AudioInputStream streamExtract = soundHelper.getStreamExtract(audioFile.getAudioInputStream(), (int) playlistItemFragment.getExtractStartInSeconds(),
        (int) playlistItemFragment.getExtractDurationInSeconds());
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
