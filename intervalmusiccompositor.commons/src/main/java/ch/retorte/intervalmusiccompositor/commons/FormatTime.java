package ch.retorte.intervalmusiccompositor.commons;

import java.text.DecimalFormat;

/**
 * Formats time.
 */
public class FormatTime {

  private static final String NUMBER_FORMAT = "00";
  private static final String SIGN = "-";

  /**
   * Takes seconds as argument and formats them to for time display ('MM:SS', or 'H:MM:SS' if there are hours).
   *
   * @param seconds
   *          , a positive number of seconds, if not integer it is rounded.
   * @return the formatted string
   */
  public String getStrictFormattedTime(double seconds) {

    int discreteSeconds = (int) Math.abs(seconds);

    String result = "";
    DecimalFormat df = new DecimalFormat(NUMBER_FORMAT);

    if (discreteSeconds < 3600) {
      int minutes = (int) Math.floor(discreteSeconds / 60.0);
      result = df.format(minutes) + ":" + df.format(discreteSeconds - (minutes * 60));
    } else {
      int hours = (int) Math.floor(discreteSeconds / 3600.0);
      int minutes = (int) Math.floor((discreteSeconds - (hours * 3600)) / 60.0);
      result = hours + ":" + df.format(minutes) + ":" + df.format(discreteSeconds - (minutes * 60) - (hours * 3600));
    }

    if (seconds < 0) {
      result = SIGN + result;
    }

    return result;
  }

}
