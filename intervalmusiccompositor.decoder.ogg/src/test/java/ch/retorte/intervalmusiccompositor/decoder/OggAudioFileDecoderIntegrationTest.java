package ch.retorte.intervalmusiccompositor.decoder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;

/**
 * Integration test for the {@link OggAudioFileDecoder}.
 * 
 * @author nw
 */
public class OggAudioFileDecoderIntegrationTest {

  AudioStandardizer normalizer = Mockito.mock(AudioStandardizer.class);

  @Before
  public void setup() {
    when(normalizer.standardize(Mockito.any(AudioInputStream.class))).then(new Answer<AudioInputStream>() {
      @Override
      public AudioInputStream answer(InvocationOnMock invocation) throws Throwable {
        return (AudioInputStream) invocation.getArguments()[0];
      }
    });
  }

  @Test
  public void shouldDecodeOgg() throws UnsupportedAudioFileException, IOException {
    // given
    URL resource = getClass().getClassLoader().getResource("10s_silence.ogg");
    File oggFile = new File(resource.getFile());

    // when
    AudioInputStream decodedStream = new OggAudioFileDecoder(normalizer).decode(oggFile);

    // then
    assertThat(decodedStream.getFormat().getEncoding(), is(AudioFormat.Encoding.PCM_SIGNED));
    assertThat(decodedStream.getFormat().getChannels(), is(2));
    assertThat(decodedStream.getFormat().getFrameRate(), is(44100f));
    assertThat(decodedStream.getFormat().getFrameSize(), is(4));
    assertThat(decodedStream.getFormat().getSampleRate(), is(44100f));
    assertThat(decodedStream.getFormat().getSampleSizeInBits(), is(16));
  }

}
