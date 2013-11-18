package edu.achriste.ics683.image;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.imaging.ImageFormat.IMAGE_FORMAT_TIFF;

/**
 * Provides extra functionality on top of a BufferedImage for easily modifying the contents of a BufferedImage.
 * @author Anthony Christe
 */
public class EditableImage {
  private BufferedImage image;
  private int padding;

  /**
   * Create an EditableImage from a given BufferedImage.
   * @param image A BufferedImage.
   */
  public EditableImage(BufferedImage image) {
    this.image = image;
    this.padding = 0;
  }

  /**
   * Create an EditableImage from a given image file.
   * Uses the Apache Commons Imaging library to read into a BufferedImage.
   * @param file The image file.
   */
  public EditableImage(File file) {
    try {
      this.image = Imaging.getBufferedImage(file);
      if(image == null) {
        throw new IOException();
      }
    } catch (IOException e) {
      System.out.println("Could not load image file " + file);
      e.printStackTrace();
    }
    catch (ImageReadException e) {
      System.out.println("Could not load image file " + file);
      e.printStackTrace();
    }
  }

  /**
   * Returns the height of an image.
   * @return The height of an image.
   */
  public int getHeight() {
    return image.getHeight();
  }

  /**
   * Returns the width of an image.
   * @return The width of an image.
   */
  public int getWidth() {
    return image.getWidth();
  }

  /**
   * Returns the total number of pixels in an image.
   * @return The total number of pixels in an image.
   */
  public int getSize() {
    return image.getHeight() * image.getWidth();
  }

  /**
   * Returns the grayscale value (0 - 255) of the image at the given x and y-coordinates.
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return The grayscale value (0 - 255) of the image at the given x and y-coordinates.
   */
  public int getGrayscale(int x, int y) {
    return grayscaleFromRgb(image.getRGB(x, y));
  }

  /**
   * Sets the grayscale value (0 - 255) of the image at the given x and y-coordinates.
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @param value The grayscale value (0 - 255).
   */
  public void setGrayscale(int x, int y, int value) {
    image.setRGB(x, y, rgbFromGrayscale(value));
  }

  /**
   * Converts a color value in rgb color space to its grayscale equivalent (0 - 255).
   * @param rgb RGB value is rgb color space.
   * @return The converted grayscale value.
   */
  public static int grayscaleFromRgb(int rgb) {
    return (rgb >> 16) & 0xFF;
  }

  /**
   * Converts a grayscale value (0 - 255) into a value in rgb color space.
   * @param grayscale The grayscale value (0 - 255).
   * @return An rgb value in rgb color space.
   */
  public static int rgbFromGrayscale(int grayscale) {
    return (grayscale << 16) | (grayscale << 8) | grayscale | (255 << 24);
  }

  /**
   * Returns the BufferedImage associated with this.
   * @return The BufferedImage associated with this.
   */
  public BufferedImage getImage() {
    return this.image;
  }

  /**
   * Returns a copy of this EditableImage.
   * @return A copy of this EditableImage.
   */
  public EditableImage copy() {
    ColorModel colorModel = image.getColorModel();
    boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
    WritableRaster writeableRaster = image.copyData(null);
    return new EditableImage(new BufferedImage(colorModel, writeableRaster, isAlphaPremultiplied, null));
  }

  /**
   * Pads this image with zeroes.
   * The image is padded on all sides by the amount n.
   * @param n The length to pad on each side.
   */
  public void padWithZeros(int n) {
    BufferedImage paddedImage;
    this.padding = n;
    int paddedWidth = image.getWidth() + (2 * n);
    int paddedHeight = image.getHeight() + (2 * n);

    paddedImage = new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_BYTE_INDEXED);

    // First set everything to 0
    for(int r = 0; r < paddedImage.getHeight(); r++) {
      for(int c = 0; c < paddedImage.getWidth(); c++) {
        paddedImage.setRGB(c, r, rgbFromGrayscale(0));
      }
    }

    // Insert original image into padded image
    for(int r = 0; r < image.getHeight(); r++) {
      for(int c = 0; c < image.getWidth(); c++) {
        paddedImage.setRGB(c + n, r + n, image.getRGB(c, r));
      }
    }

    // Finally, update the image
    this.image = paddedImage;
  }

  /**
   * Removes padding from this image.
   */
  public void removePadding() {
    BufferedImage unpaddedImage;

    int width = image.getWidth() - (2 * this.padding);
    int height = image.getHeight() - (2 * this.padding);

    unpaddedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);

    // Insert original image into padded image
    for(int r = padding; r < image.getHeight() - padding; r++) {
      for(int c = padding; c < image.getWidth() - padding; c++) {
        unpaddedImage.setRGB(c - padding, r - padding, image.getRGB(c, r));
      }
    }

    // Finally, update the image
    this.image = unpaddedImage;

    // Reset the padding
    this.padding = 0;
  }

  /**
   * Writes an image to a file.
   * Uses the Apache Commons Imaging library to write to a file.
   * @param fileName The file name and location to write to.
   */
  public void writeImage(String fileName) {
    try {
      Imaging.writeImage(image, new File(fileName), IMAGE_FORMAT_TIFF, null);
    } catch (ImageWriteException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
