package ch.retorte.intervalmusiccompositor.decoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import ch.retorte.intervalmusiccompositor.decoder.ogg.OggFileProperties;
import org.junit.Test;

/**
 * Test cases for the {@link OggFileProperties}.
 * 
 * @author nw
 */
public class OggFilePropertiesTest {

  File oggFile = mockFileWithName("mySoundFile.ogg");
  File uppercaseOggFile = mockFileWithName("mySoundFile.OGG");
  File otherFile = mockFileWithName("mySoundFile.xyz");

  private File mockFileWithName(String name) {
    File result = mock(File.class);
    when(result.getName()).thenReturn(name);
    return result;
  }

  @Test
  public void shouldBeOfRightType() {
    assertTrue(new OggFileProperties().isOfThisType(oggFile));
    assertTrue(new OggFileProperties().isOfThisType(uppercaseOggFile));
  }

  @Test
  public void shouldNotBeOfRightType() {
    assertFalse(new OggFileProperties().isOfThisType(otherFile));
  }
}
