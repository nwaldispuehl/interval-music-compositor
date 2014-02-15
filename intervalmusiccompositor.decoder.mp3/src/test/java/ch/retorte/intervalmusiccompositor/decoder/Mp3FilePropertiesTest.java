package ch.retorte.intervalmusiccompositor.decoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;

/**
 * Test cases for the {@link Mp3FileProperties}.
 * 
 * @author nw
 */
public class Mp3FilePropertiesTest {

  File mp3File = mockFileWithName("mySoundFile.mp3");
  File uppercaseMp3File = mockFileWithName("mySoundFile.MP3");
  File otherFile = mockFileWithName("mySoundFile.xyz");

  private File mockFileWithName(String name) {
    File result = mock(File.class);
    when(result.getName()).thenReturn(name);
    return result;
  }

  @Test
  public void shouldBeOfRightType() {
    assertTrue(new Mp3FileProperties().isOfThisType(mp3File));
    assertTrue(new Mp3FileProperties().isOfThisType(uppercaseMp3File));
  }

  @Test
  public void shouldNotBeOfRightType() {
    assertFalse(new Mp3FileProperties().isOfThisType(otherFile));
  }
}
