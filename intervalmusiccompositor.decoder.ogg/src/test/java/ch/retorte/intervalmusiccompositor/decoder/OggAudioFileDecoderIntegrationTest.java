package ch.retorte.intervalmusiccompositor.decoder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;

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
    assertThat(decodedStream.getFormat().getEncoding(), is(AudioFormat.Encoding.PCM_SIGNED));
    assertThat(decodedStream.getFormat().getChannels(), is(2));
    assertThat(decodedStream.getFormat().getFrameRate(), is(44100f));
    assertThat(decodedStream.getFormat().getFrameSize(), is(4));
    assertThat(decodedStream.getFormat().getSampleRate(), is(44100f));
    assertThat(decodedStream.getFormat().getSampleSizeInBits(), is(16));
  }

}
