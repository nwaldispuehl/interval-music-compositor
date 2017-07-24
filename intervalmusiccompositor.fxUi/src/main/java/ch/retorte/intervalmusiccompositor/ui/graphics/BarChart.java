package ch.retorte.intervalmusiccompositor.ui.graphics;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;


import java.util.Collection;
import java.util.List;

/**
 * BarChart class. Used to generate bars with different widths and a specified spacing between.
 * 
 * @author nw
 */
public class BarChart {

  // / The image where the bar chart is drawn.
  private WritableImage image;

  /**
   * Constructor.
   * 
   * @param width
   *          Width for bar chart.
   * @param height
   *          Height for bar chart.
   */
  public BarChart(Integer width, Integer height) {
    image = new WritableImage(width, height);
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
    Color color = Color.rgb(red, green, blue);

    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        image.getPixelWriter().setColor(j, i, color);
      }
    }
  }

  /**
   * Generates the bar chart.
   */
  public void generate(List<Integer> soundPattern, List<Integer> breakPattern, Integer iterations, Boolean hasBreakTrack) {

    Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
    GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
    graphicsContext.setFontSmoothingType(FontSmoothingType.LCD);
    graphicsContext.drawImage(image, 0, 0);

    double absoluteWidth = 0;

    for (int i = 0; i < soundPattern.size(); i++) {

      // Add current pattern item
      absoluteWidth += soundPattern.get(i);

      // Add current break item (or restart if there are not enough of them)
      if (has(breakPattern)) {
        absoluteWidth += breakPattern.get(i % breakPattern.size());
      }
    }

    absoluteWidth *= iterations;

    Font soundFont = Font.font("Sans Serif", FontWeight.BOLD, 10);
    FontMetrics soundFontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(soundFont);

    Font breakFont = Font.font("Sans Serif", FontWeight.NORMAL, 9);
    FontMetrics breakFontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(breakFont);

    Color darkBlue = Color.rgb(55, 119, 248);
    Color lightBlue = Color.rgb(113, 174, 243);
    Color evenLighterBlue = Color.rgb(182, 216, 255);
    Color textColor = Color.GRAY;

    double top = 0;
    double bottom = Math.max(soundFontMetrics.getLineHeight(), breakFontMetrics.getLineHeight()) * 1.2;
    double border = 0;
    double p = border;

    double scale = (image.getWidth() - (2.0 * border)) / absoluteWidth;

    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < soundPattern.size(); j++) {

        // Add next sound pattern item

        graphicsContext.setFont(soundFont);

        double width = soundPattern.get(j) * scale;

        graphicsContext.setFill(lightBlue);
        graphicsContext.fillRect(p, top, width, image.getHeight() - top - bottom);
        graphicsContext.setFill(darkBlue);
        graphicsContext.strokeRect(p, top, width, image.getHeight() - top - bottom);

        graphicsContext.setFill(textColor);
        float labelWidth = soundFontMetrics.computeStringWidth(soundPattern.get(j).toString());
        if (labelWidth < width) {
          graphicsContext.fillText(soundPattern.get(j).toString(), p + (width - labelWidth) / 2, image.getHeight() - soundFontMetrics.getDescent());
        }
        p = p + width;

        // Add next break pattern item
        graphicsContext.setFont(breakFont);

        int breakWidth = 0;
        if (has(breakPattern)) {
          breakWidth = (int) (breakPattern.get(j % breakPattern.size()) * scale);
        }

        // We only paint the break if there is a break track
        if (hasBreakTrack) {
          graphicsContext.setFill(evenLighterBlue);
          graphicsContext.fillRect(p, top, breakWidth, image.getHeight() - top - bottom);
          graphicsContext.setFill(lightBlue);
          graphicsContext.strokeRect(p, top, breakWidth, image.getHeight() - top - bottom);
        }

        graphicsContext.setFill(textColor);

        float breakLabelWidth = 0;
        if (has(breakPattern)) {
          breakLabelWidth = breakFontMetrics.computeStringWidth(breakPattern.get(j % breakPattern.size()).toString());
        }
        if (breakLabelWidth < breakWidth) {
          graphicsContext.fillText(breakPattern.get(j % breakPattern.size()).toString(), p + (breakWidth - breakLabelWidth) / 2, image.getHeight()
              - breakFontMetrics.getDescent());
        }


        if (has(breakPattern)) {
          p = p + (breakPattern.get(j % breakPattern.size()) * scale);
        }
      }
    }

    image = canvas.snapshot(null, null);
  }

  private boolean has(Collection<?> collection) {
    return !collection.isEmpty();
  }

  /**
   * Returns the bar chart as image.
   * 
   * @return bar chart image.
   */
  public WritableImage getWritableImage() {
    return image;
  }

}
