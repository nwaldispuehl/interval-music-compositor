package ch.retorte.intervalmusiccompositor.util;

import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import org.junit.jupiter.api.Test;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the {@link ThreadHelper}.
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
