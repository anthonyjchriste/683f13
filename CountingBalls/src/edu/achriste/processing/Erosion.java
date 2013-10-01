package edu.achriste.processing;

import edu.achriste.utils.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * Contains methods for eroding an image using a disk structuring element.
 *
 * @author Anthony Christe
 */
public class Erosion {
  /**
   * Moves the disk structuring element over every pixel in the image and erodes it if all foreground pixels in the
   * structuring element match foreground pixels in the image.
   * @param image The image to erode.
   * @param disk The disk structuring element to use.
   * @return The eroded image.
   */
  public static BufferedImage erode(BufferedImage image, DiskSE disk) {
    BufferedImage copiedImage = ImageUtils.copyBufferedImage(image);

    for (int r = 0; r < image.getHeight(); r++) {
      for (int c = 0; c < image.getWidth(); c++) {
        // Structuring element foreground pixels all match image foreground pixels. Set black
        if (canOrAll(image, disk, r, c)) {
          copiedImage.setRGB(c, r, ImageUtils.rgbFromGrayscale(0));
        }
        // Not all pixels match. Set white
        else {
          copiedImage.setRGB(c, r, ImageUtils.rgbFromGrayscale(255));
        }
      }
    }
    return copiedImage;
  }

  /**
   * Checks if all foreground pixels in a disk structuring element match foreground pixels in an image starting at a
   * particular row and column.
   * @param image The image to place the structuring element onto.
   * @param disk The structuring element.
   * @param row The row to place the top left of the structuring element onto.
   * @param col The column to place the top left of the structuring element onto.
   * @return true if all foreground pixels in the structuring element match a foreground pixel in the underlying image,
   *         or false otherwise.
   */
  private static boolean canOrAll(BufferedImage image, DiskSE disk, int row, int col) {
    for (int r = 0; r < disk.getHeight(); r++) {
      for (int c = 0; c < disk.getWidth(); c++) {
        if (
                // Make sure we're in bounds
                (row + r < image.getHeight() && (col + c < image.getWidth())) &&
                // If disk pixel is foreground
                disk.get(r, c) == 1 &&
                // If image pixel is foreground
                ImageUtils.binaryFromRgb(image.getRGB(c + col, r + row)) != 1
            ) {
          return false;
        }
      }
    }
    return true;
  }
}
