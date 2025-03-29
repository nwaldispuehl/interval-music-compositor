package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.decoder.mp3.Mp3AudioFileDecoder;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test for the {@link Mp3AudioFileDecoder}.
 * 
 * @author nw
 */
public class Mp3AudioFileDecoderIntegrationTest {

  @Test
  public void shouldDecodeMp3() throws UnsupportedAudioFileException, IOException {
    // given
    URL resource = getClass().getClassLoader().getResource("10s_silence.mp3");
    File mp3File = new File(resource.getFile());

    // when
    AudioInputStream decodedStream = new Mp3AudioFileDecoder().decode(mp3File);

    // then
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, decodedStream.getFormat().getEncoding());
    assertEquals(1, decodedStream.getFormat().getChannels());
    assertEquals(44100f, decodedStream.getFormat().getFrameRate());
    assertEquals(2, decodedStream.getFormat().getFrameSize());
    assertEquals(44100f, decodedStream.getFormat().getSampleRate());
    assertEquals(16, decodedStream.getFormat().getSampleSizeInBits());
  }

}
