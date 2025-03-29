package ch.retorte.intervalmusiccompositor.ui.graphics;

import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;
//import com.sun.javafx.tk.FontMetrics;
//import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


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
    fill("#f5f5f5");
  }

  /**
   * Fills the background of the bar chart.
   * 
   * @param webColor
   *          the color in hex representation (e.g. '#123456')
   */
  private void fill(String webColor) {
    Color color = Color.web(webColor);

    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        image.getPixelWriter().setColor(j, i, color);
      }
    }
  }

  /**
   * Generates the bar chart.
   */
  public void generate(List<Integer> soundPattern, List<Integer> breakPattern, Integer iterations, List<SoundEffectOccurrence> soundEffectOccurrences, Boolean hasBreakTrack) {

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
//    Bounds soundFontHeight = Toolkit.getToolkit().getFontLoader().getFontMetrics(soundFont);

    Font breakFont = Font.font("Sans Serif", FontWeight.NORMAL, 9);
//    Bounds breakFontBounds = Toolkit.getToolkit().getFontLoader().getFontMetrics(breakFont);

    Color darkBlue = Color.web("#2196F3");
    Color lightBlue = Color.web("#03A9F4");
    Color evenLighterBlue = Color.web("#81D4FA");
    Color soundEffectsColor = Color.web("#8BC34A");
    Color textColor = Color.GRAY;

    double top = 0;
    double bottom = Math.max(computeBoundsOf(soundFont, "M").getHeight(), computeBoundsOf(breakFont, "M").getHeight()) * 1.2;
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
        double labelWidth = computeBoundsOf(soundFont, soundPattern.get(j).toString()).getWidth();
        if (labelWidth < width) {
//          graphicsContext.fillText(soundPattern.get(j).toString(), p + (width - labelWidth) / 2, image.getHeight() - soundFontMetrics.getDescent());
          graphicsContext.fillText(soundPattern.get(j).toString(), p + (width - labelWidth) / 2, image.getHeight() - 2);
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

        double breakLabelWidth = 0;
        if (has(breakPattern)) {
          breakLabelWidth = computeBoundsOf(breakFont, breakPattern.get(j % breakPattern.size()).toString()).getWidth();
        }
        if (breakLabelWidth < breakWidth) {
//          graphicsContext.fillText(breakPattern.get(j % breakPattern.size()).toString(), p + (breakWidth - breakLabelWidth) / 2, image.getHeight() - breakFontMetrics.getDescent());
          graphicsContext.fillText(breakPattern.get(j % breakPattern.size()).toString(), p + (breakWidth - breakLabelWidth) / 2, image.getHeight() - 2);
        }


        if (has(breakPattern)) {
          p = p + (breakPattern.get(j % breakPattern.size()) * scale);
        }
      }
    }

    // Add sound effects
    // We iterate again over all iterations to keep the sound effects in front

    if (!soundEffectOccurrences.isEmpty()) {
      double currentLeftBorder = border;

      for (int i = 0; i < iterations; i++) {
        for (int j = 0; j < soundPattern.size(); j++) {

          double width = soundPattern.get(j) * scale;

          for (SoundEffectOccurrence s : soundEffectOccurrences) {
            double soundEffectPosition = s.getStartTimeMs() / 1000.0 * scale;
            double soundEffectWidth = s.getSoundEffect().getDisplayDurationMs() / 1000.0 * scale;

            graphicsContext.setFill(soundEffectsColor);
            graphicsContext.fillRect(currentLeftBorder + soundEffectPosition, top, soundEffectWidth, image.getHeight() - top - bottom);
            graphicsContext.setFill(lightBlue);
            graphicsContext.strokeRect(currentLeftBorder + soundEffectPosition, top, soundEffectWidth, image.getHeight() - top - bottom);
          }

          currentLeftBorder = currentLeftBorder + width;

          if (has(breakPattern)) {
            currentLeftBorder = currentLeftBorder + (breakPattern.get(j % breakPattern.size()) * scale);
          }

        }
      }
    }

    image = canvas.snapshot(null, null);
  }

  private Bounds computeBoundsOf(Font font, String text) {
    Text theText = new Text(text);
    theText.setFont(font);
    return theText.getBoundsInLocal();


//    return Toolkit.getToolkit().getFontLoader().getFontMetrics(font).computeStringWidth(text);
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
