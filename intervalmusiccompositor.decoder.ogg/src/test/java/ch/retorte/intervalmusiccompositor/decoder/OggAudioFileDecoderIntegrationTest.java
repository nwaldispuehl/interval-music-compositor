package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.decoder.ogg.OggAudioFileDecoder;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test for the {@link OggAudioFileDecoder}.
 * 
 * @author nw
 */
public class OggAudioFileDecoderIntegrationTest {

  @Test
  public void shouldDecodeOgg() throws UnsupportedAudioFileException, IOException {
    // given
    URL resource = getClass().getClassLoader().getResource("10s_silence.ogg");
    File oggFile = new File(resource.getFile());

    // when
    AudioInputStream decodedStream = new OggAudioFileDecoder().decode(oggFile);

    // then
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, decodedStream.getFormat().getEncoding());
    assertEquals(2, decodedStream.getFormat().getChannels());
    assertEquals(44100f, decodedStream.getFormat().getFrameRate());
    assertEquals(4, decodedStream.getFormat().getFrameSize());
    assertEquals(44100f,  decodedStream.getFormat().getSampleRate());
    assertEquals(16, decodedStream.getFormat().getSampleSizeInBits());
  }

}
