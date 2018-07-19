package ch.retorte.intervalmusiccompositor.player;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioFileMusicPlayer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * Knows how to play back an extract of an {@link IAudioFile}.
 */
public class ExtractMusicPlayer extends StreamMusicPlayer implements AudioFileMusicPlayer {

  private MessageFormatBundle bundle = getBundle("core_imc");

  private MessageProducer messageProducer;
  private SoundHelper soundHelper;

  public ExtractMusicPlayer(MessageProducer messageProducer) {
    super(messageProducer);
    this.messageProducer = messageProducer;
    this.soundHelper = new SoundHelper(messageProducer);
  }

  @Override
  public void play(IAudioFile audioFile) {
    if (isPlaying()) {
      return;
    }

    addDebugMessage("Started playing: " + audioFile.getDisplayName());

    int audioFileDurationInSeconds = (int) (audioFile.getDuration() / 1000);

    // Load random audio input stream extract of this track
    int extractLength = Integer.valueOf(bundle.getString("imc.audio.determine_bpm.trackLength"));

    if (!audioFile.isLongEnoughFor(extractLength)) {
      extractLength = audioFileDurationInSeconds;
    }

    int extractStart = (audioFileDurationInSeconds - extractLength) / 2;

    AudioInputStream inputStream = null;
    try {
      inputStream = soundHelper.getLeveledStream(soundHelper.getStreamExtract(audioFile.getAudioInputStream(), extractStart, extractLength),
          audioFile.getVolumeRatio(), 1);
    }
    catch (IOException e) {
      messageProducer.send(new ErrorMessage(e.getMessage()));
      addDebugMessage(e);
    }

    if (inputStream != null) {
      play(inputStream);
    }
  }

}
