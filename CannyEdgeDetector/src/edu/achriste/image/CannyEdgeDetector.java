package edu.achriste.image;

import org.apache.commons.math3.analysis.function.Gaussian;

public class CannyEdgeDetector {
  private EditableImage image;
  private double sigma;
  private double[][][] gradients;

  private final int GRADIENT_X = 0;
  private final int GRADIENT_Y = 1;
  private final int GRADIENT_STRENGTH = 2;
  private final int GRADIENT_STRENGTH_NORM = 3;
  private final int GRADIENT_DIRECTION = 4;

  public CannyEdgeDetector(EditableImage image, double sigma) {
    this.image = image;
    this.sigma = sigma;
  }

  public void applyGaussianFilter() {
    double[] kernel = getGaussianKernel();
    int size = (int)(2 * sigma);
    int halfSize = size / 2;
    double val;

    // First, pad the image
    image.padWithZeros(halfSize);

    for(int r = 0; r < image.getHeight() - kernel.length + 1; r++) {
      for(int c = 0; c < image.getWidth() - kernel.length + 1; c++) {
        val = convolude(kernel, r, c);
        image.setGrayscale(c + (halfSize), r + (halfSize), (int) val);
      }
    }

    image.removePadding();
  }

  public void applyFeatureDetection() {
    gradients = new double[image.getHeight()][image.getWidth()][5];
    double norm = 0;

    double[] sobelXX = {1, 2, 1};
    double[] sobelXY = {-1, 0, 1};
    double[] sobelYX = {1, 0, -1};
    double[] sobelYY = {1, 2, 1};

    // Pad the image
    image.padWithZeros(1);

    for(int r = 0; r < image.getHeight() - 2; r++) {
      for(int c = 0; c < image.getWidth() - 2; c++) {
        gradients[r][c][GRADIENT_X] = convolude(sobelXX, sobelXY, r, c);
        gradients[r][c][GRADIENT_Y] = convolude(sobelYX, sobelYY, r, c);
        gradients[r][c][GRADIENT_STRENGTH] = Math.sqrt(Math.pow(gradients[r][c][GRADIENT_X], 2) + Math.pow(gradients[r][c][GRADIENT_Y], 2));
        gradients[r][c][GRADIENT_DIRECTION] = roundAngle(Math.atan2(gradients[r][c][GRADIENT_Y], gradients[r][c][GRADIENT_X]));
        norm += gradients[r][c][GRADIENT_STRENGTH];
      }
    }

    // Find normalized gradient strength
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        gradients[r][c][GRADIENT_STRENGTH_NORM] = gradients[r][c][GRADIENT_STRENGTH] / norm;
      }
    }

    // Remove padding
    image.removePadding();

    for(int r = 0; r < image.getHeight() - 2; r++) {
      for(int c = 0; c < image.getWidth() - 2; c++) {
        image.setGrayscale(c, r, (int)gradients[r][c][GRADIENT_STRENGTH]);
      }
    }
  }

  private double convolude(double[] kernel, int row, int col) {
    return this.convolude(kernel, kernel, row, col);
  }

  public double convolude(double[] kernelX, double[] kernelY, int row, int col) {
    double[] tmp = new double[kernelX.length];
    double sum = 0;

    for(int r = 0; r < kernelX.length; r++) {
      for(int c = 0; c < kernelX.length; c++) {
        sum += kernelX[c] * image.getGrayscale(c + col, r + row);
      }
      tmp[r] = sum;
      sum = 0;
    }

    for(int i = 0; i < kernelY.length; i++) {
      sum += kernelY[i] * tmp[i];
    }

    return sum;
  }

  private int roundAngle(double rads) {
    double degs = Math.toDegrees(rads);

    // Make sure we're using a positive angle
    if(degs < 0) {
      degs = 180 + degs;
    }

    if((degs >= 0 && degs < 22.5) || (degs >= 157.5 && degs <= 180)) {
      degs = 0;
    }
    else if((degs >= 22.5 && degs < 67.5)){
      degs = 45;
    }
    else if((degs >= 67.5 && degs < 112.5)){
      degs = 90;
    }
    else if((degs >= 112.5 && degs < 157.5)){
      degs = 90;
    }

    return (int) degs;
  }

  private double[] getGaussianKernel() {
    int size = (int) (2 * sigma);
    double norm = 0;
    Gaussian gaussian = new Gaussian(size / 2, sigma);
    double[] kernel = new double[size];

    for(int i = 0; i < kernel.length; i++) {
      double val = gaussian.value(i);
      kernel[i] = val;
      norm += val;
    }

    for(int i = 0; i < kernel.length; i++) {
      kernel[i] /= norm;
    }

    return kernel;
  }
}
