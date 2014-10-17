package ch.retorte.intervalmusiccompositor.encoder;

import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;

/**
 * @author nw
 */
public class LameByteArrayEncoder {

  private static final boolean USE_VARIABLE_BITRATE = false;
  private static final int GOOD_QUALITY_BITRATE = 256;

  private AudioFormat inputFormat;

  public LameByteArrayEncoder(AudioFormat inputFormat) {
    this.inputFormat = inputFormat;
  }

  public byte[] encodePcmToMp3(byte[] pcm) {
    LameEncoder encoder = new LameEncoder(inputFormat, GOOD_QUALITY_BITRATE, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, USE_VARIABLE_BITRATE);

    ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
    byte[] buffer = new byte[encoder.getPCMBufferSize()];

    int bytesToTransfer = Math.min(buffer.length, pcm.length);
    int bytesWritten;
    int currentPcmPosition = 0;
    while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
      currentPcmPosition += bytesToTransfer;
      bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);

      mp3.write(buffer, 0, bytesWritten);
    }

    encoder.close();
    return mp3.toByteArray();
  }
}
