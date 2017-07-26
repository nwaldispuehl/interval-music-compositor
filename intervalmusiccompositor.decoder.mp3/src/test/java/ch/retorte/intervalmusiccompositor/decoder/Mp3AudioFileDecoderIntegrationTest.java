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
    assertThat(decodedStream.getFormat().getEncoding(), is(AudioFormat.Encoding.PCM_SIGNED));
    assertThat(decodedStream.getFormat().getChannels(), is(1));
    assertThat(decodedStream.getFormat().getFrameRate(), is(44100f));
    assertThat(decodedStream.getFormat().getFrameSize(), is(2));
    assertThat(decodedStream.getFormat().getSampleRate(), is(44100f));
    assertThat(decodedStream.getFormat().getSampleSizeInBits(), is(16));
  }

}
