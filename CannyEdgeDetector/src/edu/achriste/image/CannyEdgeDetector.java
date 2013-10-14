package edu.achriste.image;

import org.apache.commons.math3.analysis.function.Gaussian;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class CannyEdgeDetector {
  private EditableImage image;
  private double sigma;
  private double[][][] gradients;

  private final int GRADIENT_X = 0;
  private final int GRADIENT_Y = 1;
  private final int GRADIENT_STRENGTH = 2;
  private final int GRADIENT_STRENGTH_NORM = 3;
  private final int GRADIENT_DIRECTION = 4;
  private final int HYSTERESIS = 5;

  private Map<Integer, EditableImage> imageMap;
  public static final int IMAGE_GAUSSIAN = 0;
  public static final int IMAGE_GRADIENT_X = 1;
  public static final int IMAGE_GRADIENT_Y = 2;
  public static final int IMAGE_GRADIENT_X_NORM = 3;
  public static final int IMAGE_GRADIENT_Y_NORM = 4;
  public static final int IMAGE_GRADIENT_STRENGTH = 5;
  public static final int IMAGE_GRADIENT_STRENGTH_NORM = 6;
  public static final int IMAGE_NON_MAXIMUM_SUPPRESSION = 7;
  public static final int IMAGE_HIGH_THRESHOLD = 8;
  public static final int IMAGE_BETWEEN_THRESHOLD = 9;
  public static final int IMAGE_HYSTERESIS = 10;

  private EditableImage imageGaussian;
  private EditableImage imageGradientX;
  private EditableImage imageGradientY;
  private EditableImage imageGradientXNorm;
  private EditableImage imageGradientYNorm;
  private EditableImage imageGradientStrength;
  private EditableImage imageGradientStrengthNorm;
  private EditableImage imageNonMaximumSuppression;
  private EditableImage imageHighThreshold;
  private EditableImage imageBetweenThreshold;
  private EditableImage imageHysteresis;

  public CannyEdgeDetector(EditableImage image, double sigma) {
    this.image = image;
    this.sigma = sigma;
    this.imageMap = new HashMap<Integer, EditableImage>();

    gradients = new double[image.getHeight()][image.getWidth()][7];

    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        gradients[r][c][HYSTERESIS] = -1;
      }
    }

    imageGaussian = new EditableImage(image.copy().getImage());
    imageGradientX = new EditableImage(image.copy().getImage());
    imageGradientY = new EditableImage(image.copy().getImage());
    imageGradientXNorm = new EditableImage(image.copy().getImage());
    imageGradientYNorm = new EditableImage(image.copy().getImage());
    imageGradientStrength = new EditableImage(image.copy().getImage());
    imageGradientStrengthNorm = new EditableImage(image.copy().getImage());
    imageNonMaximumSuppression = new EditableImage(image.copy().getImage());
    imageHighThreshold = new EditableImage(image.copy().getImage());
    imageBetweenThreshold = new EditableImage(image.copy().getImage());
    imageHysteresis = new EditableImage(image.copy().getImage());

    imageMap.put(IMAGE_GAUSSIAN, imageGaussian);
    imageMap.put(IMAGE_GRADIENT_X, imageGradientX);
    imageMap.put(IMAGE_GRADIENT_Y, imageGradientY);
    imageMap.put(IMAGE_GRADIENT_X_NORM, imageGradientXNorm);
    imageMap.put(IMAGE_GRADIENT_Y_NORM, imageGradientYNorm);
    imageMap.put(IMAGE_GRADIENT_STRENGTH, imageGradientStrength);
    imageMap.put(IMAGE_GRADIENT_STRENGTH_NORM, imageGradientStrengthNorm);
    imageMap.put(IMAGE_NON_MAXIMUM_SUPPRESSION, imageNonMaximumSuppression);
    imageMap.put(IMAGE_HIGH_THRESHOLD, imageHighThreshold);
    imageMap.put(IMAGE_BETWEEN_THRESHOLD, imageBetweenThreshold);
    imageMap.put(IMAGE_HYSTERESIS, imageHysteresis);

    applyGaussianFilter();
    applyFeatureDetection();
    applyNonMaximumSuppression();
    applyHysteresis(0.1, 0.3);
  }

  private void applyGaussianFilter() {
    double[] kernel = getGaussianKernel();
    int size = (int) (2 * sigma);
    int halfSize = size / 2;
    double val;

    // First, pad the image
    imageGaussian.padWithZeros(halfSize);

    for (int r = 0; r < imageGaussian.getHeight() - kernel.length + 1; r++) {
      for (int c = 0; c < imageGaussian.getWidth() - kernel.length + 1; c++) {
        val = convolude(imageGaussian, kernel, r, c);
        imageGaussian.setGrayscale(c + (halfSize), r + (halfSize), (int) val);
      }
    }

    imageGaussian.removePadding();
  }

  private double[] getGaussianKernel() {
    int size = (int) (2 * sigma);
    double norm = 0;
    Gaussian gaussian = new Gaussian(size / 2, sigma);
    double[] kernel = new double[size];

    for (int i = 0; i < kernel.length; i++) {
      double val = gaussian.value(i);
      kernel[i] = val;
      norm += val;
    }

    for (int i = 0; i < kernel.length; i++) {
      kernel[i] /= norm;
    }

    return kernel;
  }

  private void applyFeatureDetection() {
    EditableImage tmpImage = imageGaussian.copy();


    double[] sobelXX = {1, 2, 1};
    double[] sobelXY = {-1, 0, 1};
    double[] sobelYX = {1, 0, -1};
    double[] sobelYY = {1, 2, 1};

    // Pad the image
    tmpImage.padWithZeros(1);
    for (int r = 0; r < image.getHeight() - 2; r++) {
      for (int c = 0; c < image.getWidth() - 2; c++) {
        gradients[r][c][GRADIENT_X] = convolude(tmpImage, sobelXX, sobelXY, r, c);
        gradients[r][c][GRADIENT_Y] = convolude(tmpImage, sobelYX, sobelYY, r, c);
      }
    }
    tmpImage.removePadding();

    double minX = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;
    double minStrength = Double.MAX_VALUE;
    double maxStrength = Double.MIN_VALUE;

    for (int r = 0; r < gradients.length; r++) {
      for (int c = 0; c < gradients[r].length; c++) {
        gradients[r][c][GRADIENT_STRENGTH] = Math.sqrt(Math.pow(gradients[r][c][GRADIENT_X], 2) + Math.pow(gradients[r][c][GRADIENT_Y], 2));
        gradients[r][c][GRADIENT_DIRECTION] = roundAngle(Math.atan2(gradients[r][c][GRADIENT_Y], gradients[r][c][GRADIENT_X]));

        minX = gradients[r][c][GRADIENT_X] < minX ? gradients[r][c][GRADIENT_X] : minX;
        maxX = gradients[r][c][GRADIENT_X] > maxX ? gradients[r][c][GRADIENT_X] : maxX;
        minY = gradients[r][c][GRADIENT_Y] < minY ? gradients[r][c][GRADIENT_Y] : minY;
        maxY = gradients[r][c][GRADIENT_Y] > maxY ? gradients[r][c][GRADIENT_Y] : maxY;
        minStrength = gradients[r][c][GRADIENT_STRENGTH] < minStrength ? gradients[r][c][GRADIENT_STRENGTH] : minStrength;
        maxStrength = gradients[r][c][GRADIENT_STRENGTH] > maxStrength ? gradients[r][c][GRADIENT_STRENGTH] : maxStrength;
      }
    }

    for (int r = 0; r < gradients.length; r++) {
      for (int c = 0; c < gradients[r].length; c++) {
        gradients[r][c][GRADIENT_STRENGTH_NORM] = (gradients[r][c][GRADIENT_STRENGTH] - minStrength) / (maxStrength - minStrength);
        imageGradientX.setGrayscale(c, r, (int) gradients[r][c][GRADIENT_X]);
        imageGradientY.setGrayscale(c, r, (int) gradients[r][c][GRADIENT_Y]);
        imageGradientXNorm.setGrayscale(c, r, (int) ((gradients[r][c][GRADIENT_X] - minX) / (maxX - minX) * 255));
        imageGradientYNorm.setGrayscale(c, r, (int) ((gradients[r][c][GRADIENT_Y] - minY) / (maxY - minY) * 255));
        imageGradientStrength.setGrayscale(c, r, (int) gradients[r][c][GRADIENT_STRENGTH]);
        imageGradientStrengthNorm.setGrayscale(c, r, (int) ((gradients[r][c][GRADIENT_STRENGTH] - minStrength) / (maxStrength - minStrength) * 255));
      }
    }
  }

  private int roundAngle(double rads) {
    double degs = Math.toDegrees(rads);

    // Make sure we're using a positive angle
    if (degs < 0) {
      degs = 180 + degs;
    }

    if ((degs >= 0 && degs < 22.5) || (degs >= 157.5 && degs <= 180)) {
      degs = 0;
    } else if ((degs >= 22.5 && degs < 67.5)) {
      degs = 45;
    } else if ((degs >= 67.5 && degs < 112.5)) {
      degs = 90;
    } else if ((degs >= 112.5 && degs < 157.5)) {
      degs = 135;
    }

    return (int) degs;
  }

  private void applyNonMaximumSuppression() {
    imageNonMaximumSuppression = imageGradientStrengthNorm.copy();

    for (int r = 0; r < gradients.length; r++) {
      for (int c = 0; c < gradients[r].length; c++) {
        switch ((int) gradients[r][c][GRADIENT_DIRECTION]) {
          case 0:
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r - 1, c, r + 1, c)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
          case 45:
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r - 1, c - 1, r + 1, c + 1)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
          case 90:
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r, c - 1, r, c + 1)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
          case 135:
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r - 1, c + 1, r + 1, c - 1)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
        }
      }
    }
    imageMap.put(IMAGE_NON_MAXIMUM_SUPPRESSION, imageNonMaximumSuppression);
  }

  private boolean shouldSuppress(int gradStrength, int r1, int c1, int r2, int c2) {
    if (r1 < gradients.length && r1 >= 0 && c1 < gradients[r1].length && c1 >= 0) {
      if (gradStrength < gradients[r1][c1][GRADIENT_STRENGTH]) {
        return true;
      }
    }

    if (r2 < gradients.length && r2 >= 0 && c2 < gradients[r2].length && c2 >= 0) {
      if (gradStrength < gradients[r2][c2][GRADIENT_STRENGTH]) {
        return true;
      }
    }

    return false;
  }

  private void applyHysteresis(double t1, double t2) {
    // First, set all values less than t1 to black
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        if(gradients[r][c][GRADIENT_STRENGTH_NORM] < t1) {
          gradients[r][c][HYSTERESIS] = 0;
        }
      }
    }

    // Second, set all values greater than t2 to white
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        if(gradients[r][c][GRADIENT_STRENGTH_NORM] > t2) {
          gradients[r][c][HYSTERESIS] = 255;
        }
      }
    }

    // Third, trace from t1s to t2s
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        if(gradients[r][c][HYSTERESIS] == 255) {
          applyHysteresis(r - 1, c, t1, t2);
          applyHysteresis(r - 1, c + 1, t1, t2);
          applyHysteresis(r, c + 1, t1, t2);
          applyHysteresis(r + 1, c + 1, t1, t2);
          applyHysteresis(r + 1, c, t1, t2);
          applyHysteresis(r + 1, c - 1, t1, t2);
          applyHysteresis(r, c - 1, t1, t2);
          applyHysteresis(r - 1, c - 1, t1, t2);
        }
      }
    }

    // Set any remaining pixels to black
    // Also set between thresholds image
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        if(gradients[r][c][HYSTERESIS] == 127 || gradients[r][c][HYSTERESIS] == -1) {
          imageBetweenThreshold.setGrayscale(c, r, 127);
        }
        else {
          imageBetweenThreshold.setGrayscale(c, r, 0);
        }
        if(gradients[r][c][HYSTERESIS] == -1) {
          gradients[r][c][HYSTERESIS] = 0;
        }
      }
    }

    // Set high and hysteresis images
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        if(gradients[r][c][HYSTERESIS] == 255) {
          imageHighThreshold.setGrayscale(c, r, 255);
        }
        else {
          imageHighThreshold.setGrayscale(c, r, 0);
        }
        imageHysteresis.setGrayscale(c, r, (int) gradients[r][c][HYSTERESIS]);
      }
    }
  }

  private void applyHysteresis(int r, int c, double t1, double t2) {
    if(r < 0 || r >= gradients.length) {
      return;
    }
    if(c < 0 || c >= gradients[r].length) {
      return;
    }
    if(gradients[r][c][HYSTERESIS] != -1) {
      return;
    }

    gradients[r][c][HYSTERESIS] = 127;
    applyHysteresis(r - 1, c, t1, t2);
    applyHysteresis(r - 1, c + 1, t1, t2);
    applyHysteresis(r, c + 1, t1, t2);
    applyHysteresis(r + 1, c + 1, t1, t2);
    applyHysteresis(r + 1, c, t1, t2);
    applyHysteresis(r + 1, c - 1, t1, t2);
    applyHysteresis(r, c - 1, t1, t2);
    applyHysteresis(r - 1, c - 1, t1, t2);
  }

  private double convolude(EditableImage convoludeImage, double[] kernel, int row, int col) {
    return this.convolude(convoludeImage, kernel, kernel, row, col);
  }

  private double convolude(EditableImage convoludeImage, double[] kernelX, double[] kernelY, int row, int col) {
    double[] tmp = new double[kernelX.length];
    double sum = 0;

    for (int r = 0; r < kernelX.length; r++) {
      for (int c = 0; c < kernelX.length; c++) {
        sum += kernelX[c] * convoludeImage.getGrayscale(c + col, r + row);
      }
      tmp[r] = sum;
      sum = 0;
    }

    for (int i = 0; i < kernelY.length; i++) {
      sum += kernelY[i] * tmp[i];
    }

    return sum;
  }

  public EditableImage getImage(final int IMAGE_TYPE) {
    return imageMap.get(IMAGE_TYPE);
  }
}
