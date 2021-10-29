package ch.retorte.intervalmusiccompositor.decoder.ogg;

import java.io.File;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import ch.retorte.intervalmusiccompositor.spi.bpm.BPMReaderWriter;

/**
 * {@link BPMReaderWriter} for the OGG audio format.
 */
public class OggBPMReaderWriter implements BPMReaderWriter {

  private final OggFileProperties oggFile = new OggFileProperties();

  @Override
  public Integer readBPMFrom(File file) {

    try {
      org.jaudiotagger.audio.AudioFile f = AudioFileIO.read(file);
      VorbisCommentTag ovtag = (VorbisCommentTag) f.getTag();
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
      VorbisCommentTag ovtag = (VorbisCommentTag) f.getTag();

      ovtag.setField(ovtag.createField(VorbisCommentFieldKey.BPM, String.valueOf(bpm)));

      f.commit();
    } catch (Exception e) {
      // nop
    }
  }

  @Override
  public boolean isAbleToReadWriteBPM(File file) {
    return oggFile.isOfThisType(file);
  }

}
