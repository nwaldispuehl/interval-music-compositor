package ch.retorte.intervalmusiccompositor.encoder.mp3;

import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author nw
 */
class LameByteArrayEncoder {

  private static final boolean USE_VARIABLE_BITRATE = false;
  private static final int GOOD_QUALITY_BITRATE = 256;

  private AudioFormat inputFormat;
  private ProgressListener progressListener;

  LameByteArrayEncoder(AudioFormat inputFormat, ProgressListener progressListener) {
    this.inputFormat = inputFormat;
    this.progressListener = progressListener;
  }

  byte[] encodeToMp3(AudioInputStream audioInputStream, long streamLengthInBytes) throws IOException {
    LameEncoder encoder = new LameEncoder(inputFormat, GOOD_QUALITY_BITRATE, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, USE_VARIABLE_BITRATE);
    ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
    byte[] inputBuffer = new byte[encoder.getPCMBufferSize()];
    byte[] outputBuffer = new byte[encoder.getPCMBufferSize()];

    int bytesRead;
    int bytesWritten;
    int currentPcmPosition = 0;

    while(0 < (bytesRead = audioInputStream.read(inputBuffer))) {
      bytesWritten = encoder.encodeBuffer(inputBuffer, 0, bytesRead, outputBuffer);
      currentPcmPosition += bytesRead;
      updateProgressWith(currentPcmPosition, streamLengthInBytes);
      mp3.write(outputBuffer, 0, bytesWritten);
    }

    encoder.close();
    return mp3.toByteArray();
  }

  private void updateProgressWith(long currentPosition, long total) {
    progressListener.onProgressUpdate((int) (100.0 / total * currentPosition));
  }
}
