package ch.retorte.intervalmusiccompositor.ui.gfx;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;

/**
 * BarChart class. Used to generate bars with different widths and a specified spacing between.
 * 
 * @author nw
 */
public class BarChart {

  private MessageFormatBundle bundle = getBundle("ui_imc");

  // / The image where the bar chart is drawn.
  private BufferedImage image;

  /**
   * Constructor.
   * 
   * @param width
   *          Width for bar chart.
   * @param height
   *          Height for bar chart.
   */
  public BarChart(Integer width, Integer height) {
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    fill(245, 245, 245);
  }

  /**
   * Fills the background of the bar chart.
   * 
   * @param red
   *          Red value for background (0-255)
   * @param green
   *          Green value for background (0-255)
   * @param blue
   *          Blue value for background (0-255)
   */
  private void fill(int red, int green, int blue) {
    int rgb = new Color(red, green, blue).getRGB();
    for (int i = 0; i < image.getHeight(); i++)
      for (int j = 0; j < image.getWidth(); j++)
        image.setRGB(j, i, rgb);
  }

  /**
   * Generates the bar chart.
   */
  public void generate(List<Integer> soundPattern, List<Integer> breakPattern, Integer iterations, Boolean hasBreakTrack) {
    Graphics graphics = image.createGraphics();

    if (soundPattern == null || soundPattern.isEmpty() || breakPattern == null || breakPattern.isEmpty() || iterations == 0
        || image.getWidth() < (iterations * (soundPattern.size() + breakPattern.size()))) {
      fill(245, 245, 245);

      // Print out of scope message in the center of the image
      Font errorFont = new Font("Sans Serif", Font.BOLD, 12);
      FontMetrics errorFontMetrics = graphics.getFontMetrics(errorFont);
      graphics.setColor(Color.black);
      String errorString = bundle.getString("ui.preview_chart.out_of_scope");
      int errorLabelWidth = errorFontMetrics.stringWidth(errorString);
      graphics.drawString(errorString, (image.getWidth() - errorLabelWidth) / 2, (image.getHeight() + errorFontMetrics.getDescent()) / 2);

      return;
    }

    int absoluteWidth = 0;

    for (int i = 0; i < soundPattern.size(); i++) {

      // Add current pattern item
      absoluteWidth += soundPattern.get(i);

      // Add current break item (or restart if there are not enough of them)
      absoluteWidth += breakPattern.get(i % breakPattern.size());
    }

    absoluteWidth *= iterations;

    Font soundFont = new Font("Sans Serif", Font.BOLD, 10);
    FontMetrics soundFontMetrics = graphics.getFontMetrics(soundFont);

    Font breakFont = new Font("Sans Serif", Font.PLAIN, 9);
    FontMetrics breakFontMetrics = graphics.getFontMetrics(breakFont);

    Color darkBlue = new Color(55, 119, 248);
    Color lightBlue = new Color(113, 174, 243);
    Color evenLighterBlue = new Color(182, 216, 255);
    Color textColor = Color.GRAY;

    int top = 0;
    int bottom = Math.max(soundFontMetrics.getHeight(), breakFontMetrics.getHeight());
    int border = 0;
    int p = border;

    double scale = (image.getWidth() - (2.0 * border)) / absoluteWidth;

    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < soundPattern.size(); j++) {

        // Add next sound pattern item

        graphics.setFont(soundFont);

        int width = (int) (soundPattern.get(j) * scale);

        graphics.setColor(lightBlue);
        graphics.fillRect(p, top, width, image.getHeight() - top - bottom);
        graphics.setColor(darkBlue);
        graphics.drawRect(p, top, width, image.getHeight() - top - bottom);

        graphics.setColor(textColor);

        int labelWidth = soundFontMetrics.stringWidth(soundPattern.get(j).toString());
        if (labelWidth < width) {
          graphics.drawString(soundPattern.get(j).toString(), p + (width - labelWidth) / 2, image.getHeight() - soundFontMetrics.getDescent());
        }
        p = p + width;

        // Add next break pattern item
        graphics.setFont(breakFont);

        int breakWidth = (int) (breakPattern.get(j % breakPattern.size()) * scale);

        // We only paint the break if there is a break track
        if (hasBreakTrack) {
          graphics.setColor(evenLighterBlue);
          graphics.fillRect(p, top, breakWidth, image.getHeight() - top - bottom);
          graphics.setColor(lightBlue);
          graphics.drawRect(p, top, breakWidth, image.getHeight() - top - bottom);
        }

        graphics.setColor(textColor);

        int breakLabelWidth = breakFontMetrics.stringWidth(breakPattern.get(j % breakPattern.size()).toString());
        if (breakLabelWidth < breakWidth) {
          graphics.drawString(breakPattern.get(j % breakPattern.size()).toString(), p + (breakWidth - breakLabelWidth) / 2, image.getHeight()
              - breakFontMetrics.getDescent());
        }

        p = p + (int) (breakPattern.get(j % breakPattern.size()) * scale);
      }
    }
  }

  /**
   * Returns the bar chart as image.
   * 
   * @return bar chart image.
   */
  public BufferedImage getBufferedImage() {
    return image;
  }

}