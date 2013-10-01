package edu.achriste.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class EditableImage {
  private BufferedImage image;

  public EditableImage(BufferedImage image) {
    this.image = image;
  }

  public EditableImage(File file) {
    try {
      this.image = ImageIO.read(file);
      if(image == null) {
        throw new IOException();
      }
    } catch (IOException e) {
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
    return (image.getRGB(x, y) >> 16) & 0xFF;
  }

  public void setGrayscale(int x, int y, int value) {
    int grayscale =  (value << 16) | (value << 8) | value | (255 << 24);
    image.setRGB(x, y, grayscale);
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


}
