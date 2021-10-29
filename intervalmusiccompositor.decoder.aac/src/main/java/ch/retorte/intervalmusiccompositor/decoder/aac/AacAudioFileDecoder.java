package ch.retorte.intervalmusiccompositor.decoder.aac;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import ch.retorte.intervalmusiccompositor.spi.decoder.AudioFileDecoder;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;

/**
 * Decodes AAC files.
 */
public class AacAudioFileDecoder implements AudioFileDecoder {

  //---- Fields

  private final AacFileProperties aacFile = new AacFileProperties();


  //---- Methods

  @Override
  public AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, IOException {
    AudioInputStream result = null;

    /* We cascade different methods here since every one is usable for another type of aac file. */
    try {
      result = new AACAudioFileReader().getAudioInputStream(inputFile);
    }
    catch (Exception e) {
      // nop
    }
    
    if (result == null) {
      try {
        result = decodeAAC(inputFile);
      }
      catch (Exception e) {
        result = decodeMP4(inputFile);
      }
    }

    return result;
  }

  @Override
  public boolean isAbleToDecode(File file) {
    return aacFile.isOfThisType(file);
  }

  @Override
  public Collection<String> getExtensions() {
    return newArrayList(aacFile.getFileExtensions());
  }

  private AudioInputStream decodeAAC(File inputFile) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    AudioFormat audioFormat;

    try {
      final ADTSDemultiplexer adts = new ADTSDemultiplexer(new FileInputStream(inputFile));
      final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());

      final SampleBuffer buf = new SampleBuffer();
      byte[] b;
      while (true) {
        try {
          b = adts.readNextFrame();
        }
        catch (Exception e) {
          break;
        }

        dec.decodeFrame(b, buf);
        outputStream.write(buf.getData());
      }

      audioFormat = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, buf.isBigEndian());

    } finally {
      // nop
    }

    byte[] outputStreamByteArray = outputStream.toByteArray();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStreamByteArray);

    return new AudioInputStream(inputStream, audioFormat, outputStreamByteArray.length);
  }

  private AudioInputStream decodeMP4(File inputFile) throws UnsupportedAudioFileException, IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    AudioFormat audioFormat;

    try {

      RandomAccessFile randomAccessFile = new RandomAccessFile(inputFile, "r");
      final MP4Container cont = new MP4Container(randomAccessFile);
      final Movie movie = cont.getMovie();
      final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
      if (tracks.isEmpty()) {
        throw new UnsupportedAudioFileException("Movie does not contain any AAC track");
      }

      final AudioTrack track = (AudioTrack) tracks.get(0);
      final Decoder dec = new Decoder(track.getDecoderSpecificInfo());

      Frame frame;
      final SampleBuffer buf = new SampleBuffer();
      while (track.hasMoreFrames()) {
        frame = track.readNextFrame();
        dec.decodeFrame(frame.getData(), buf);
        outputStream.write(buf.getData());
      }

      audioFormat = new AudioFormat(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
    } finally {
      // nop
    }

    byte[] outputStreamByteArray = outputStream.toByteArray();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStreamByteArray);

    return new AudioInputStream(inputStream, audioFormat, outputStreamByteArray.length);
  }
}
