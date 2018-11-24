package ch.retorte.intervalmusiccompositor.decoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import ch.retorte.intervalmusiccompositor.decoder.aac.AacFileProperties;
import org.junit.Test;

/**
 * Test cases for the {@link AacFileProperties}.
 * 
 * @author nw
 */
public class AacFilePropertiesTest {

  File aacFile = mockFileWithName("mySoundFile.aac");
  File uppercaseAacFile = mockFileWithName("mySoundFile.AAC");
  File m4aFile = mockFileWithName("mySoundFile.m4a");
  File mp4File = mockFileWithName("mySoundFile.mp4");
  File otherFile = mockFileWithName("mySoundFile.xyz");

  private File mockFileWithName(String name) {
    File result = mock(File.class);
    when(result.getName()).thenReturn(name);
    return result;
  }

  @Test
  public void shouldBeOfRightType() {
    assertTrue(new AacFileProperties().isOfThisType(aacFile));
    assertTrue(new AacFileProperties().isOfThisType(uppercaseAacFile));
    assertTrue(new AacFileProperties().isOfThisType(m4aFile));
    assertTrue(new AacFileProperties().isOfThisType(mp4File));
  }

  @Test
  public void shouldNotBeOfRightType() {
    assertFalse(new AacFileProperties().isOfThisType(otherFile));
  }
}
