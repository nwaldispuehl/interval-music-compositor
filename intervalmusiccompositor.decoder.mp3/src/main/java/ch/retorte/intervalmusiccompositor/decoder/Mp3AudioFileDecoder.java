package ch.retorte.intervalmusiccompositor.decoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Decodes MP3 files.
 */
public class Mp3AudioFileDecoder implements AudioFileDecoder {

  //---- Fields

  private Mp3FileProperties mp3File = new Mp3FileProperties();


  //---- Methods

  @Override
  public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {

    MpegAudioFileReader mafr = new MpegAudioFileReader();
    AudioInputStream mp3Ais;
    // AudioInputStream mp3Dmais = null;
    AudioInputStream result;

    try {
      mp3Ais = mafr.getAudioInputStream(inputFile);
    } catch (Exception e) {

      /*
       * This is now a work around (aka hack) for files with stored images in them. The images have to be removed. We load the file as FileInputStream, check
       * the length of the header and skip it.
       */
      FileInputStream f_in = new FileInputStream(inputFile);
      Bitstream m = new Bitstream(f_in);
      long start = m.header_pos();

      try {
        m.close();
      } catch (BitstreamException be) {
        // nop
      }

      f_in = new FileInputStream(inputFile);

      // Skip the header
      f_in.skip(start);

      // Now try again with the 'truncated' sound stream
      mp3Ais = mafr.getAudioInputStream(f_in);
    }

    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, mp3Ais.getFormat().getSampleRate(), 16, mp3Ais.getFormat().getChannels(),
        mp3Ais.getFormat().getChannels() * 2, mp3Ais.getFormat().getSampleRate(), false);

    result = new DecodedMpegAudioInputStream(decodedFormat, mp3Ais);

    // result = new PCMtoPCMCodec().getAudioInputStream(TARGET_ENCODING, mp3Dmais);
    // result = new MpegFormatConversionProvider().getAudioInputStream(TARGET_ENCODING, mp3Dmais);
    // result = AudioSystem.getAudioInputStream(TARGET_ENCODING, mp3Dmais);

    return tidyStream(result);
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return mp3File.isOfThisType(file);
  }

  @Override
  public Collection<String> getExtensions() {
    return newArrayList(mp3File.getFileExtensions());
  }

  /**
   * Creates a new, fresh {@link AudioInputStream} from an existing one. This showed to cure some MP3-related problems.
   */
  private AudioInputStream tidyStream(AudioInputStream ais) {

    AudioInputStream result = null;

    try {
      // First read stream into byte array
      byte[] stream = getByteArrayOfUndefStream(ais);

      // Then create new AudioInputStream from byte array
      result = new AudioInputStream(new ByteArrayInputStream(stream), ais.getFormat(), AudioSystem.NOT_SPECIFIED);
    } catch (IOException e) {
      // nop
    }

    return result;
  }

  private byte[] getByteArrayOfUndefStream(AudioInputStream inputStream) throws IOException {

    ArrayList<byte[]> totalBuffer = new ArrayList<>();

    int size = 10240000;
    byte[] metaBuffer = new byte[size];
    totalBuffer.add(metaBuffer);

    // Choose a buffer of 100 KB
    byte[] buffer = new byte[102400];

    int len;
    int writtenBytes = 0;
    while ((len = inputStream.read(buffer)) != -1) {

      if ((writtenBytes + len) <= size) {

        System.arraycopy(buffer, 0, metaBuffer, writtenBytes, len);
        writtenBytes += len;

      } else {

        int remainingBytes = size - writtenBytes;
        int outstandingBytes = len - remainingBytes;

        // Copy remaining bytes to metaBuffer
        System.arraycopy(buffer, 0, metaBuffer, writtenBytes, remainingBytes);

        // Change metaBuffer
        metaBuffer = new byte[size];
        totalBuffer.add(metaBuffer);

        // Copy rest bytes to new buffer
        System.arraycopy(buffer, remainingBytes, metaBuffer, 0, outstandingBytes);

        writtenBytes = outstandingBytes;
      }
    }

    // Assemble to result array
    byte[] result = new byte[((totalBuffer.size() - 1) * size) + writtenBytes];

    for (int i = 0; i < totalBuffer.size(); i++) {
      int bytesToRead = size;
      if (i == totalBuffer.size() - 1) {
        bytesToRead = writtenBytes;
      }
      System.arraycopy(totalBuffer.get(i), 0, result, size * i, bytesToRead);
    }

    return result;
  }

}
