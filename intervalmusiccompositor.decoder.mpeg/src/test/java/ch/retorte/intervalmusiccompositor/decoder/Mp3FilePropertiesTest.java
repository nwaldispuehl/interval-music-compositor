package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.decoder.mp3.Mp3FileProperties;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
