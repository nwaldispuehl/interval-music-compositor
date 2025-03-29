package ch.retorte.intervalmusiccompositor.decoder;

import ch.retorte.intervalmusiccompositor.decoder.flac.FlacFileProperties;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for the {@link FlacFileProperties}.
 * 
 * @author nw
 */
public class FlacFilePropertiesTest {

  File flacFile = mockFileWithName("mySoundFile.flac");
  File uppercaseFlacFile = mockFileWithName("mySoundFile.flac");
  File otherFile = mockFileWithName("mySoundFile.xyz");

  private File mockFileWithName(String name) {
    File result = mock(File.class);
    when(result.getName()).thenReturn(name);
    return result;
  }

  @Test
  public void shouldBeOfRightType() {
    assertTrue(new FlacFileProperties().isOfThisType(flacFile));
    assertTrue(new FlacFileProperties().isOfThisType(uppercaseFlacFile));
  }

  @Test
  public void shouldNotBeOfRightType() {
    assertFalse(new FlacFileProperties().isOfThisType(otherFile));
  }
}
