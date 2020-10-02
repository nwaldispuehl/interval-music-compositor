package ch.retorte.intervalmusiccompositor.decoder.wave;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.retorte.intervalmusiccompositor.decoder.wave.WaveAudioFileDecoder;
import org.junit.Test;

/**
 * Integration test case for the {@link WaveAudioFileDecoder}.
 * 
 * @author nw
 */
public class WaveAudioFileDecoderIntegrationTest {

  @Test
  public void shouldDecodeWave() throws UnsupportedAudioFileException, IOException {
    // given
    URL resource = getClass().getClassLoader().getResource("10s_silence.wav");
    File wavFile = new File(resource.getFile());

    // when
    AudioInputStream decodedStream = new WaveAudioFileDecoder().decode(wavFile);

    // then
    assertThat(decodedStream.getFormat().getEncoding(), is(AudioFormat.Encoding.PCM_SIGNED));
    assertThat(decodedStream.getFormat().getChannels(), is(1));
    assertThat(decodedStream.getFormat().getFrameRate(), is(44100f));
    assertThat(decodedStream.getFormat().getFrameSize(), is(2));
    assertThat(decodedStream.getFormat().getSampleRate(), is(44100f));
    assertThat(decodedStream.getFormat().getSampleSizeInBits(), is(16));
  }

}
