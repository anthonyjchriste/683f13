package edu.achriste.image;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.imaging.ImageFormat.IMAGE_FORMAT_TIFF;


public class EditableImage {
  private BufferedImage image;
  private int padding;

  public EditableImage(BufferedImage image) {
    this.image = image;
    this.padding = 0;
  }

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

  public int getHeight() {
    return image.getHeight();
  }

  public int getWidth() {
    return image.getWidth();
  }

  public int getSize() {
    return image.getHeight() * image.getWidth();
  }

  public int getGrayscale(int x, int y) {
    return grayscaleFromRgb(image.getRGB(x, y));
  }

  public void setGrayscale(int x, int y, int value) {
    image.setRGB(x, y, rgbFromGrayscale(value));
  }

  public static int grayscaleFromRgb(int rgb) {
    return (rgb >> 16) & 0xFF;
  }

  public static int rgbFromGrayscale(int grayscale) {
    return (grayscale << 16) | (grayscale << 8) | grayscale | (255 << 24);
  }

  public BufferedImage getImage() {
    return this.image;
  }

  public EditableImage copy() {
    ColorModel colorModel = image.getColorModel();
    boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
    WritableRaster writeableRaster = image.copyData(null);
    return new EditableImage(new BufferedImage(colorModel, writeableRaster, isAlphaPremultiplied, null));
  }

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

    this.padding = 0;
  }

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
