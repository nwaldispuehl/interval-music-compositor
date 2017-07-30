package ch.retorte.intervalmusiccompositor.player;

import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.audio.MusicPlayer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * Music player which can play back an {@link AudioInputStream}.
 */
public class StreamMusicPlayer implements Runnable, MusicPlayer {

  private static final int AUDIO_BUFFER_SIZE = 32768;

  private AudioInputStream audioInputStream;
  private Boolean play = false;
  private MessageProducer messageProducer;

  StreamMusicPlayer(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  @Override
  public void run() {
    play = true;
    playInternal(audioInputStream);
  }

  @Override
  public void stop() {
    play = false;
  }

  Boolean isPlaying() {
    return play;
  }

  @Override
  public void play(AudioInputStream audioInputStream) {
    this.audioInputStream = audioInputStream;
    new Thread(this).start();
  }

  private void playInternal(AudioInputStream audioInputStream) {
    addDebugMessage("Start playing audio stream.");
    try {
      AudioFormat audioFormat = audioInputStream.getFormat();

      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
      SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(audioFormat);
      line.start();

      int nBytesRead = 0;
      byte[] abData = new byte[AUDIO_BUFFER_SIZE];

      while (nBytesRead != -1 && play) {
        try {
          nBytesRead = audioInputStream.read(abData, 0, abData.length);
        }
        catch (IOException e) {
          addDebugMessage("Unable to read data stream: " + e.getMessage());
        }
        if (nBytesRead >= 0) {
          line.write(abData, 0, nBytesRead);
        }
      }

      if (play) {
        // If we're still playing we wait until all data has been played back before proceeding and cutting off the line.
        line.drain();
      }

      line.stop();
      line.close();

      play = false;

      clearStream();
    }
    catch (LineUnavailableException e) {
      addDebugMessage("Unable to play music: " + e.getMessage());
    }

    addDebugMessage("Stopped playing audio stream.");
  }

  private void clearStream() {
    if (audioInputStream != null) {
      try {
        audioInputStream.close();
      }
      catch (IOException e) {
        // We don't do anything in this case.
      }
    }
  }

  protected void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  protected void addDebugMessage(Throwable throwable) {
    messageProducer.send(new DebugMessage(this, throwable));
  }

}
