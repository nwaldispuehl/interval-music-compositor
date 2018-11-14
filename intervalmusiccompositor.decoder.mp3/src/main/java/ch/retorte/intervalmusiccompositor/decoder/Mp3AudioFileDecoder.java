package ch.retorte.intervalmusiccompositor.decoder;

import java.io.*;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
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
    MpegAudioFileReader mpegAudioFileReader = new MpegAudioFileReader();
    AudioInputStream audioInputStream;

    try {
      audioInputStream = mpegAudioFileReader.getAudioInputStream(inputFile);
    } catch (Exception e) {

      // Try again with a 'truncated' sound stream.
      audioInputStream = mpegAudioFileReader.getAudioInputStream(streamWithoutHeaderOf(inputFile));
    }

    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        audioInputStream.getFormat().getSampleRate(),
        16,
        audioInputStream.getFormat().getChannels(),
        audioInputStream.getFormat().getChannels() * 2,
        audioInputStream.getFormat().getSampleRate(),
        false);

    return new DecodedMpegAudioInputStream(decodedFormat, audioInputStream);
  }

  /**
   * Returns an {@link InputStream} of the provided file without any meta data header.
   *
   *  This is now a work around (aka hack) for audio files with stored images in them.
   *  The images have to be removed. We load the file as FileInputStream, check the length of the header and skip it.
   */
  private InputStream streamWithoutHeaderOf(File inputFile) throws IOException {
    FileInputStream f_in = new FileInputStream(inputFile);
    Bitstream m = new Bitstream(f_in);
    long headerLength = m.header_pos();

    try {
      m.close();
    } catch (BitstreamException be) {
      // nop
    }

    f_in = new FileInputStream(inputFile);

    // Skip the header
    f_in.skip(headerLength);

    return f_in;
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return mp3File.isOfThisType(file);
  }

  @Override
  public Collection<String> getExtensions() {
    return newArrayList(mp3File.getFileExtensions());
  }

}
