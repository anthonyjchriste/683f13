package edu.achriste.image;



public class CannyEdgeDetector {
  private EditableImage image;

  public CannyEdgeDetector(EditableImage image) {
    this.image = image;
    applyGaussianFilter(1);
  }

  private void applyGaussianFilter(double sigma) {
    int filterSize = 5;//(int) (2 * sigma);

    // First, pad the image
    image.padWithZeros(0);

    for(int i = 0; i < filterSize; i++) {
      for(int j = 0; j < filterSize;j++) {
        System.out.print(get2DGaussian(sigma, i, j) + " ");
      }
      System.out.println();
    }
  }

  private double getGausian(double sigma, int xOrY) {
    double lhs = 1 / Math.sqrt(2 * Math.PI * sigma);
    double rhs = Math.exp(-((xOrY * xOrY) / (2 * (sigma * sigma))));
    return lhs * rhs;
  }

  private double get2DGaussian(double sigma, int x, int y) {
    double lhs = 1 / (2 * Math.PI * (sigma * sigma));
    double rhs = Math.exp(-(((x * x) + (y * y)) / (2 * (sigma * sigma))));
    return lhs * rhs;
  }
}
