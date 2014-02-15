package ch.retorte.intervalmusiccompositor.util;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;

/**
 * @author nw
 */
public class ThreadHelperTest {

  @Test
  public void shouldSleep() {
    // given
    ThreadHelper threadHelper = new ThreadHelper(mock(MessageProducer.class));
    long startTime;
    long endTime;

    // when
    startTime = currentTimeMillis();
    threadHelper.sleep(100);
    endTime = currentTimeMillis();

    // then
    long difference = endTime - startTime;
    assertTrue(100 <= difference);
    assertTrue(difference < 110); // Fingers crossed... :D
  }
}
