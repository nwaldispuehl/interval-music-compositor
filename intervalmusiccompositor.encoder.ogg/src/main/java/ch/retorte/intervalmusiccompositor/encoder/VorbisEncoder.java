package ch.retorte.intervalmusiccompositor.encoder;

/********************************************************************
 * *
 * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.   *
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
 * *
 * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2002             *
 * by the Xiph.Org Foundation http://www.xiph.org/                  *
 * *
 ********************************************************************/

import ch.retorte.intervalmusiccompositor.spi.progress.ProgressListener;
import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.*;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The VorbisEncoder converts a byte array of pcm wave data into the vorbis format in a ogg container.
 * <p>
 * Code of this class more or less one by one taken from the original VorbisEncoder from here:
 * http://downloads.xiph.org/releases/vorbis-java/
 */
class VorbisEncoder {

  //---- Static

  private static final String ENCODER_TAG_NAME = "ENCODER";
  private static final String ENCODER_TAG_CONTENT = "Java Vorbis Encoder";

  private static final float HIGH_QUALITY_256_KBITS = .8f;


  //---- Fields

  private ogg_stream_state oggStreamState;

  private ogg_page oggPage;
  private ogg_packet oggPacket;

  private vorbis_dsp_state vorbisDspState;  // central working state for the packet->PCM decoder
  private vorbis_block vorbisBlock;  // local working space for packet->PCM decode

  private int READ = 1024;
  private byte[] readBuffer = new byte[READ * 4 + 44];

  private AudioFormat audioFormat;
  private ProgressListener progressListener;


  //---- Constructor

  VorbisEncoder(AudioFormat audioFormat, ProgressListener progressListener) {
    this.audioFormat = audioFormat;
    this.progressListener = progressListener;
  }


  //---- Methods

  byte[] encodePcmToOgg(byte[] pcm) throws IOException {
    ByteArrayInputStream pcmInputStream = new ByteArrayInputStream(pcm);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    long bytesReadSoFar = 0;

    initializeOggStream();
    initializeOggPageFor(result);

    while (!isOggEndOfStream()) {

      int i;
      int bytes = pcmInputStream.read(readBuffer, 0, READ * 4); // stereo hardwired here

      bytesReadSoFar = bytesReadSoFar + bytes;
      updateProgressWith(bytesReadSoFar, pcm.length);

      if (0 < bytes) {

        // data to encode

        // expose the buffer to submit data
        float[][] buffer = vorbisDspState.vorbis_analysis_buffer(READ);

        // Un-interleave samples
        for (i = 0; i < bytes / 4; i++) {
          buffer[0][vorbisDspState.pcm_current + i] = ((readBuffer[i * 4 + 1] << 8) | (0x00ff & (int) readBuffer[i * 4])) / 32768.f;
          buffer[1][vorbisDspState.pcm_current + i] = ((readBuffer[i * 4 + 3] << 8) | (0x00ff & (int) readBuffer[i * 4 + 2])) / 32768.f;
        }

        // Tell the library how much we actually submitted
        vorbisDspState.vorbis_analysis_wrote(i);
      } else {

        // end of file.  this can be done implicitly in the mainline,
        // but it's easier to see here in non-clever fashion.
        // Tell the library we're at end of stream so that it can handle
        // the last frame and mark end of stream in the output properly

        vorbisDspState.vorbis_analysis_wrote(0);
      }

      // vorbis does some data preanalysis, then divides up blocks for more involved
      // (potentially parallel) processing.  Get a single block for encoding now

      while (vorbisBlock.vorbis_analysis_blockout(vorbisDspState)) {

        // analysis, assume we want to use bitrate management

        vorbisBlock.vorbis_analysis(null);
        vorbisBlock.vorbis_bitrate_addblock();

        while (vorbisDspState.vorbis_bitrate_flushpacket(oggPacket)) {

          // weld the packet into the bitstream
          oggStreamState.ogg_stream_packetin(oggPacket);

          // write out pages (if any)
          while (!isOggEndOfStream()) {

            if (!oggStreamState.ogg_stream_pageout(oggPage)) {
              break;
            }

            result.write(oggPage.header, 0, oggPage.header_len);
            result.write(oggPage.body, 0, oggPage.body_len);
          }
        }
      }
    }

    pcmInputStream.close();
    result.close();

    return result.toByteArray();
  }

  private void initializeOggStream() throws IOException {
    vorbis_info vorbisInfo = new vorbis_info();
    vorbisenc encoder = new vorbisenc();

    if (!encoder.vorbis_encode_init_vbr(vorbisInfo, audioFormat.getChannels(), (int) audioFormat.getSampleRate(), HIGH_QUALITY_256_KBITS)) {
      throw new IOException("Failed to initialize Vorbis encoder.");
    }

    vorbis_comment vorbisComment = new vorbis_comment();
    vorbisComment.vorbis_comment_add_tag(ENCODER_TAG_NAME, ENCODER_TAG_CONTENT);

    vorbisDspState = new vorbis_dsp_state();

    if (!vorbisDspState.vorbis_analysis_init(vorbisInfo)) {
      throw new IOException("Failed to initialize Vorbis DSP state.");
    }

    vorbisBlock = new vorbis_block(vorbisDspState);

    java.util.Random generator = new java.util.Random();  // need to randomize seed
    oggStreamState = new ogg_stream_state(generator.nextInt(256));

    ogg_packet header = new ogg_packet();
    ogg_packet header_comm = new ogg_packet();
    ogg_packet header_code = new ogg_packet();

    vorbisDspState.vorbis_analysis_headerout(vorbisComment, header, header_comm, header_code);

    oggStreamState.ogg_stream_packetin(header); // automatically placed in its own page
    oggStreamState.ogg_stream_packetin(header_comm);
    oggStreamState.ogg_stream_packetin(header_code);
  }

  private void initializeOggPageFor(ByteArrayOutputStream result) {
    oggPage = new ogg_page();
    oggPacket = new ogg_packet();

    while (true) {
      if (!oggStreamState.ogg_stream_flush(oggPage))
        break;

      result.write(oggPage.header, 0, oggPage.header_len);
      result.write(oggPage.body, 0, oggPage.body_len);
    }
  }

  private boolean isOggEndOfStream() {
    return 0 < oggPage.ogg_page_eos();
  }

  private void updateProgressWith(long currentPosition, long total) {
    progressListener.onProgressUpdate((int) (100.0 / total * currentPosition));
  }
}
