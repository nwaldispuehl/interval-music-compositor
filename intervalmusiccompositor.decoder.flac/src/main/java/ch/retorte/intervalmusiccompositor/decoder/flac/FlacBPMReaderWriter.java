package ch.retorte.intervalmusiccompositor.decoder.flac;

import java.io.File;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;

/**
 * @author nw
 */
public class FlacBPMReaderWriter implements BPMReaderWriter {

  private FlacFileProperties flacFile = new FlacFileProperties();

  @Override
  public Integer readBPMFrom(File file) {

    try {
      org.jaudiotagger.audio.AudioFile f = AudioFileIO.read(file);
      FlacTag tag = (FlacTag) f.getTag();
      VorbisCommentTag ovtag = tag.getVorbisCommentTag();

      String bpmTag = ovtag.getFirst(VorbisCommentFieldKey.BPM);

      if (!bpmTag.equals("")) {
        return Integer.valueOf(bpmTag);
      }
    } catch (Exception e) {
      // nop
    }

    return null;
  }

  @Override
  public void writeBPMTo(Integer bpm, File file) {

    try {
      org.jaudiotagger.audio.AudioFile f = AudioFileIO.read(file);
      FlacTag tag = (FlacTag) f.getTag();
      VorbisCommentTag ovtag = tag.getVorbisCommentTag();

      ovtag.setField(ovtag.createField(VorbisCommentFieldKey.BPM, String.valueOf(bpm)));

      f.commit();
    } catch (Exception e) {
      // nop
    }

  }

  @Override
  public boolean isAbleToReadWriteBPM(File file) {
    return flacFile.isOfThisType(file);
  }

}
