package ch.retorte.intervalmusiccompositor.compilation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Creates an envelope image from
 */
class EnvelopeImage {

  //---- Static

  private static String BACKGROUND_COLOR = "#f5f5f5";
  private static String ENVELOPE_COLOR = "#2196F3";
  private static String ENVELOPE_MEAN_COLOR = "#64B5F6";


  //---- Fields

  private WritableImage writableImage;

  private int width = 0;
  private int height = 0;


  //---- Constructor

  EnvelopeImage(Integer width, Integer height) {
    writableImage = new WritableImage(width, height);

    this.width = width;
    this.height = height;

    // Initialize image with color
    fill(BACKGROUND_COLOR);
  }


  //---- Methods

  void generateEnvelope(InputStream inputStream, long compilationDataSize, List<Integer> soundPattern, List<Integer> breakPattern, int iterations) throws IOException {
    if (compilationDataSize == 0) {
      return;
    }

    int bytesPerPixel = (int) (compilationDataSize / width);

    // Adjust samplesPerPixel to something dividable by 4 by rounding
    if (bytesPerPixel % 4 != 0) {
      bytesPerPixel = bytesPerPixel + (4 - (bytesPerPixel % 4));
    }

    int[] aggregatedAudioIntArrayLeft = new int[width];
    int[] aggregatedAudioIntArrayRight = new int[width];

    int[] aggregatedAudioIntArrayLeftMean = new int[width];
    int[] aggregatedAudioIntArrayRightMean = new int[width];

    int leftSample = 0;
    int rightSample = 0;

    int i = 0;

    byte[] buffer = new byte[bytesPerPixel];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      int currentSamples = Math.min(bytesPerPixel, bytesRead);

      aggregatedAudioIntArrayLeftMean[i] = 0;
      aggregatedAudioIntArrayRightMean[i] = 0;

      // Go through audio byte array and make an aggregated int array out of it
      for (int j = 0; j < currentSamples; j = j + 4) {

        leftSample = Math.abs(bytesToInt16(buffer, j, false));
        rightSample = Math.abs(bytesToInt16(buffer, j + 2, false));

        aggregatedAudioIntArrayLeft[i] = Math.max(aggregatedAudioIntArrayLeft[i], leftSample);
        aggregatedAudioIntArrayRight[i] = Math.max(aggregatedAudioIntArrayRight[i], rightSample);

        aggregatedAudioIntArrayLeftMean[i] += leftSample;
        aggregatedAudioIntArrayRightMean[i] += rightSample;
      }

      aggregatedAudioIntArrayLeftMean[i] = aggregatedAudioIntArrayLeftMean[i] / currentSamples * 4;
      aggregatedAudioIntArrayRightMean[i] = aggregatedAudioIntArrayRightMean[i] / currentSamples * 4;

      drawEnvelopeAmplitude(i, (double) aggregatedAudioIntArrayLeft[i] / 32767, (double) aggregatedAudioIntArrayRight[i] / 32767, ENVELOPE_COLOR);
      drawEnvelopeAmplitude(i, (double) aggregatedAudioIntArrayLeftMean[i] / 32767, (double) aggregatedAudioIntArrayRightMean[i] / 32767, ENVELOPE_MEAN_COLOR);

      i++;
    }

  }

  private void drawEnvelopeAmplitude(int x_px_position, double amp_left, double amp_right, String webColor) {

    if (1 < amp_left) {
      amp_left = 1;
    }

    if (amp_left < 0) {
      amp_left = 0;
    }

    if (1 < amp_right) {
      amp_right = 1;
    }

    if (amp_right < 0) {
      amp_right = 0;
    }

    // Calculate the respective left and right pixel values
    int centerPixel = height / 2;
    int left = centerPixel;
    int right = height - centerPixel;

    // Draw left (which is from center to top)
    for (int i = centerPixel; i > ((int) (left * (1 - amp_left))); i--) {
      setPixel(x_px_position, i, webColor);
    }

    // Draw right (which is from center to bottom)
    for (int i = centerPixel; i < centerPixel + ((int) (right * (amp_right))); i++) {
      setPixel(x_px_position, i, webColor);
    }
  }

  private void setPixel(int x, int y, String webColor) {
    writableImage.getPixelWriter().setColor(x, y, Color.web(webColor));
  }

  private void fill(String webColor) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        setPixel(j, i, webColor);
      }
    }
  }

  WritableImage getBufferedImage() {
    return writableImage;
  }

  /**
   * Taken from org.tritonus.share.sampled.TConversionTool.
   */
  private int bytesToInt16(byte[] buffer, int byteOffset, boolean bigEndian) {
    if (bigEndian) {
      return ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF));
    }
    return ((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF));
  }
}
