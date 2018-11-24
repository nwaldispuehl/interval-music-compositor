package ch.retorte.intervalmusiccompositor.decoder.wave;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;

/**
 * Test cases for the {@link WaveFileProperties}.
 * 
 * @author nw
 */
public class WaveFilePropertiesTest {

  File wavFile = mockFileWithName("mySoundFile.wav");
  File uppercaseWavFile = mockFileWithName("mySoundFile.WAV");
  File otherFile = mockFileWithName("mySoundFile.xyz");

  private File mockFileWithName(String name) {
    File result = mock(File.class);
    when(result.getName()).thenReturn(name);
    return result;
  }

  @Test
  public void shouldBeOfRightType() {
    assertTrue(new WaveFileProperties().isOfThisType(wavFile));
    assertTrue(new WaveFileProperties().isOfThisType(uppercaseWavFile));
  }

  @Test
  public void shouldNotBeOfRightType() {
    assertFalse(new WaveFileProperties().isOfThisType(otherFile));
  }
}
