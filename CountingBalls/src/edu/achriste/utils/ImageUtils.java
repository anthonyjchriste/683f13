package edu.achriste.utils;

import javax.imageio.ImageIO;
import javax.swing.JTextField;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * Provides various image utilities.
 *
 * @author Anthony Christe
 */
public class ImageUtils {
  /**
   * Reads an image file from disk and returns and BufferedImage.
   *
   * @param out The output stream to write error messages to.
   * @return A BufferedImage object of the image file.
   */
  public static BufferedImage readBufferedImage(JTextField out) {
    BufferedImage image = null;
    File fileLocation = new File("img/balls.gif");
    try {
      image = ImageIO.read(fileLocation);
    } catch (IOException e) {
      out.setText(String.format("Could not open image: %s", "img/balls.gif"));
    }
    return image;
  }

  /**
   * Returns a copy of a BufferedImage.
   * @param bufferedImage The BufferedImage to make a copy of.
   * @return A copy of the BufferedImage.
   */
  public static BufferedImage copyBufferedImage(BufferedImage bufferedImage) {
    ColorModel colorModel = bufferedImage.getColorModel();
    boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
    WritableRaster writeableRaster = bufferedImage.copyData(null);
    return new BufferedImage(colorModel, writeableRaster, isAlphaPremultiplied, null);
  }

  /**
   * Converts an integer rgb value into a grayscale value.
   * The RGB values of grayscale images are the same, so we only need to grab red value to determine the grayscale
   * value.
   * @param rgb An integer representing a rgba color value using the RGB color space.
   * @return A grayscale value such that (0 >= value <= 255).
   */
  public static int grayscaleFromRgb(int rgb) {
    return (rgb >> 16) & 0xFF;
  }

  /**
   * Converts a grayscale value into an RGB color space rgb integer.
   * @param grayscale The grayscale value such that (0 >= value <= 255)
   * @return An rgb integer in RGB color space.
   */
  public static int rgbFromGrayscale(int grayscale) {
    return getRgb(grayscale, grayscale, grayscale);
  }

  /**
   * Returns an rgb integer in RGB color space from the specified red, green, and blue values.
   * @param red The red value (0 >= red <= 255).
   * @param green The green value (0 >= green <= 255).
   * @param blue The blue value (0 >= blue <= 255).
   * @return The rgb integer value in RGB color space.
   */
  public static int getRgb(int red, int green, int blue) {
    return (red << 16) | (green << 8) | blue | (255 << 24);
  }

  /**
   * Given an integer rgb value in RGB color space, return whether or not the given pixel is foreground or background.
   * If the given value is black (0), consider it a foreground pixel and return 1. If the given pixel is not black,
   * consider it a background pixel and return a 0.
   * @param rgb An rgb integer in RGB color space.
   * @return 1 if foreground pixel, 0 otherwise.
   */
  public static int binaryFromRgb(int rgb) {
    return (grayscaleFromRgb(rgb) == 0) ? 1 : 0;
  }

  /**
   * Determines if a given pixel in the image is in the foreground (black).
   * @param image The image to test a pixel in.
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return true if the given pixel is in the foreground, false otherwise.
   */
  public static boolean isForeground(BufferedImage image, int x, int y) {
    return (binaryFromRgb(image.getRGB(x, y)) == 1);
  }

  /**
   * Returns a binary image of a given image and a given threshold.
   * All pixels less than the threshold become black and all pixels greater than the threshold become white.
   * @param image The image to create a binary image from.
   * @param threshold The threshold value to use for generating this binary image.
   * @return A binary image from the given image.
   */
  public static BufferedImage makeBinary(BufferedImage image, int threshold) {
    BufferedImage copiedImage = copyBufferedImage(image);

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        if (grayscaleFromRgb(image.getRGB(y, x)) <= threshold) {
          copiedImage.setRGB(y, x, rgbFromGrayscale(0));
        } else {
          copiedImage.setRGB(y, x, rgbFromGrayscale(255));
        }
      }
    }
    return copiedImage;
  }


}
