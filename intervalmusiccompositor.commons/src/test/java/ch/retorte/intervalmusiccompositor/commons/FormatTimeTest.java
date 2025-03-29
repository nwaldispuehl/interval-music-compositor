package ch.retorte.intervalmusiccompositor.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nw
 */
public class FormatTimeTest {

  FormatTime formatTime = new FormatTime();

  @Test
  public void shouldFormatTime() {
    testTimeFormat(0, "00:00");
    testTimeFormat(1, "00:01");
    testTimeFormat(65, "01:05");
    testTimeFormat(935, "15:35");
    testTimeFormat(3700, "1:01:40");

    testTimeFormat(-1, "-00:01");
    testTimeFormat(-65, "-01:05");
    testTimeFormat(-3700, "-1:01:40");
  }

  private void testTimeFormat(int seconds, String howShouldItLookLike) {
    assertEquals(howShouldItLookLike, formatSeconds(seconds));
  }

  private String formatSeconds(int seconds) {
    return formatTime.getStrictFormattedTime(seconds);
  }

}
