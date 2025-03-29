package ch.retorte.intervalmusiccompositor.decoder.wave;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, decodedStream.getFormat().getEncoding());
    assertEquals(1, decodedStream.getFormat().getChannels());
    assertEquals(44100f, decodedStream.getFormat().getFrameRate());
    assertEquals(2, decodedStream.getFormat().getFrameSize());
    assertEquals(44100f, decodedStream.getFormat().getSampleRate());
    assertEquals(16, decodedStream.getFormat().getSampleSizeInBits());
  }

}
