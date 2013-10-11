package edu.achriste.image;

import org.apache.commons.math3.analysis.function.Gaussian;

import java.util.Arrays;

public class CannyEdgeDetector {
  private EditableImage image;

  public CannyEdgeDetector(EditableImage image, double sigma) {
    this.image = image;
    applyGaussianFilter(sigma);
  }

  private void applyGaussianFilter(double sigma) {
    double[] kernel = get1DGaussianKernel(sigma);
    int size = (int)(2 * sigma);
    double val;

    // First, pad the image
    image.padWithZeros(size/2);

    for(int r = 0; r < image.getHeight() - kernel.length; r++) {
      for(int c = 0; c < image.getWidth() - kernel.length; c++) {
        val = convolude(kernel, r, c);
        image.setGrayscale(c + (size/2), r + (size/2), (int) val);
      }
    }

    image.removePadding();
  }

  private double convolude(double[] kernel, int row, int col) {
    double[] tmp = new double[kernel.length];
    double sum = 0;

    for(int r = 0; r < kernel.length; r++) {
      for(int c = 0; c < kernel.length; c++) {
        sum += kernel[c] * image.getGrayscale(c + col, r + row);
      }
      tmp[r] = sum;
      sum = 0;
    }

    for(int i = 0; i < kernel.length; i++) {
      sum += kernel[i] * tmp[i];
    }

    return sum;
  }

  private double[] get1DGaussianKernel(double sigma) {
    int size = (int) (2 * sigma);
    double norm = 0;
    Gaussian gaussian = new Gaussian(size / 2, sigma);
    double[] gaussianKernel = new double[size];

    for(int i = 0; i < gaussianKernel.length; i++) {
      gaussianKernel[i] = gaussian.value(i);
    }

    for(int i = 0; i < gaussianKernel.length; i++) {
      norm += gaussianKernel[i];
    }

    for(int i = 0; i < gaussianKernel.length; i++) {
      gaussianKernel[i] /= norm;
    }

    return gaussianKernel;
  }
}
