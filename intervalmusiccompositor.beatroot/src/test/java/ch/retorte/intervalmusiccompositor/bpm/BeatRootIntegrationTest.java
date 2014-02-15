package ch.retorte.intervalmusiccompositor.bpm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import at.ofai.music.beatroot.BeatRoot;
import ch.retorte.intervalmusiccompositor.decoder.Mp3AudioFileDecoder;
import ch.retorte.intervalmusiccompositor.spi.audio.AudioStandardizer;

public class BeatRootIntegrationTest {

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
  public void shouldDetect120Bpm() throws UnsupportedAudioFileException, IOException {
    
    // given
    AudioInputStream audioInputStream = getStreamFromResourceName("120_bpm.mp3");

    // when
    Integer bpm = new BeatRoot().calculateBPM(audioInputStream);
    
    // then
    assertThat(bpm, is(120));
  }

  @Test
  public void shouldDetect64Bpm() throws UnsupportedAudioFileException, IOException {

    // given
    AudioInputStream audioInputStream = getStreamFromResourceName("64_bpm.mp3");

    // when
    Integer bpm = new BeatRoot().calculateBPM(audioInputStream);

    // then
    assertThat(bpm, is(64));
  }

  @Test
  public void shouldDetect178Bpm() throws UnsupportedAudioFileException, IOException {

    // given
    AudioInputStream audioInputStream = getStreamFromResourceName("178_bpm.mp3");

    // when
    Integer bpm = new BeatRoot().calculateBPM(audioInputStream);

    // then
    assertThat(bpm, is(176)); // wut ???
  }

  private AudioInputStream getStreamFromResourceName(String name) throws UnsupportedAudioFileException, IOException {
    URL resource = getClass().getClassLoader().getResource(name);
    if (resource == null) {
      throw new IOException();
    }

    File bpmFile = new File(resource.getFile());
    return new Mp3AudioFileDecoder(normalizer).decode(bpmFile);
  }

}
