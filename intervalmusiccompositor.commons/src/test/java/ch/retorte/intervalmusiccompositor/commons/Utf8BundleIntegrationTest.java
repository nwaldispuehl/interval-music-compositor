package ch.retorte.intervalmusiccompositor.commons;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

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
    assertThat(retrievedString, is(umlautsAndAccents));

  }
}
