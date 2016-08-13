package ch.retorte.intervalmusiccompositor.player;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ErrorMessage;
import ch.retorte.intervalmusiccompositor.spi.audio.MusicPlayer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.util.SoundHelper;

/**
 * @author nw
 */
public class ExtractMusicPlayer implements Runnable, MusicPlayer {

  private static final int AUDIO_BUFFER_SIZE = 32768;

  private MessageFormatBundle bundle = getBundle("core_imc");

  private AudioInputStream inputStream;
  private Boolean play = false;
  private MessageProducer messageProducer;
  private SoundHelper soundHelper;

  public ExtractMusicPlayer(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
    this.soundHelper = new SoundHelper(messageProducer);
  }

  @Override
  public void run() {
    play = true;
    playPCM(inputStream);
  }

  public void stop() {
    play = false;
  }

  private Boolean isPlaying() {
    return play;
  }

  private void playPCM(AudioInputStream inputStream) {
    try {
      AudioFormat audioFormat = inputStream.getFormat();

      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
      SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(audioFormat);
      line.start();

      int nBytesRead = 0;
      byte[] abData = new byte[AUDIO_BUFFER_SIZE];

      while (nBytesRead != -1 && play) {
        try {
          nBytesRead = inputStream.read(abData, 0, abData.length);
        }
        catch (IOException e) {
          addDebugMessage("Unable to read data stream: " + e.getMessage());
        }
        if (nBytesRead >= 0) {
          line.write(abData, 0, nBytesRead);
        }
      }

      line.stop();
      line.close();

      play = false;

      clearStream();
    }
    catch (LineUnavailableException e) {
      addDebugMessage("Unable to play music: " + e.getMessage());
    }

    addDebugMessage("Stopped playing.");
  }

  private void clearStream() {
    if (inputStream != null) {
      try {
        inputStream.close();
      }
      catch (IOException e) {
        // We don't do anything in this case.
      }
    }
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

    try {
      inputStream = soundHelper.getLeveledStream(soundHelper.getStreamExtract(audioFile.getAudioInputStream(), extractStart, extractLength),
          audioFile.getVolumeRatio());
    }
    catch (IOException e) {
      messageProducer.send(new ErrorMessage(e.getMessage()));
      addDebugMessage(e);
    }

    if (inputStream != null) {
      new Thread(this).start();
    }
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private void addDebugMessage(Throwable throwable) {
    messageProducer.send(new DebugMessage(this, throwable));
  }

}
