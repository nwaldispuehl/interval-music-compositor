package ch.retorte.intervalmusiccompositor.decoder;

import java.io.File;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;

/**
 * @author nw
 */
public class Mp3BPMReaderWriter implements BPMReaderWriter {

  Mp3FileProperties mp3File = new Mp3FileProperties();

  @Override
  public Integer readBPMFrom(File file) {

    try {
      MP3File f = (MP3File) AudioFileIO.read(file);
      String bpmTag = "";

      if (f.hasID3v2Tag()) {
        ID3v24Tag tag = f.getID3v2TagAsv24();

        bpmTag = tag.getFirst(ID3v24FieldKey.BPM);

        if (!bpmTag.equals("")) {
          return Integer.valueOf(bpmTag);
        }
      }
    } catch (Exception e) {
      // nop
    }

    return null;
  }

  @Override
  public void writeBPMTo(Integer bpm, File file) {

    try {

      MP3File f = (MP3File) AudioFileIO.read(file);
      AbstractID3v2Tag tag = null;

      if (f.hasID3v2Tag()) {

        tag = f.getID3v2Tag();

      } else {
        tag = new ID3v24Tag();
        f.setID3v2Tag(tag);
      }

      if (tag.hasFrameOfType(ID3v24Frames.FRAME_ID_BPM)) {

        tag.setField(FieldKey.BPM, String.valueOf(bpm));

      } else {

        tag.addField(FieldKey.BPM, String.valueOf(bpm));
        tag.createField(FieldKey.BPM, String.valueOf(bpm));
        tag.setField(FieldKey.BPM, String.valueOf(bpm));
      }

      f.commit();
    } catch (Exception e) {
      // nop
    }

  }

  @Override
  public boolean isAbleToReadWriteBPM(File file) {
    return mp3File.isOfThisType(file);
  }

}
