package ch.retorte.intervalmusiccompositor.commons;

import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nw
 */
public class Utf8BundleIntegrationTest {

  @Test
  public void shouldHandleEncodedUmlauts() {
    // given
    String umlautsAndAccents = "äöüáóú";
    MessageFormatBundle testBundle = Utf8Bundle.getBundle("test");

    // when
    String retrievedString = testBundle.getString("myUmlautsAndAccents");

    // then
    assertEquals(umlautsAndAccents, retrievedString);

  }
}
