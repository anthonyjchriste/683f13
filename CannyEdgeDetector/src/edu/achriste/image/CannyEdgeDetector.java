package edu.achriste.image;



public class CannyEdgeDetector {
  private EditableImage image;

  public CannyEdgeDetector(EditableImage image) {
    this.image = image;
  }

  private void applyGaussianFilter(int filterSize) {
    // First, pad the image
    image.padWithZeros(0);
  }
}
