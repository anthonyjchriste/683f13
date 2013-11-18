package edu.achriste.image;

import org.apache.commons.math3.analysis.function.Gaussian;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides methods for finding edges of an image using the Canny Edge Detector.
 * @author Anthony Christe
 */
public class CannyEdgeDetector {
  /**
   * The original image.
   */
  private EditableImage image;

  /**
   * Sigma value to use during detection.
   */
  private double sigma;

  /**
   * Stores gradient values at pixel location r, c.
   * The third dimension (depth) of the array stores different gradient values.
   * At depth = 0, the gradient in the x-direction is stored.
   * At depth = 1, the gradient in the y-direction is stored.
   * At depth = 2, the gradient strength is stored.
   * At depth = 3, the normalized (between 0 - 1) gradient strength is stored.
   * At depth = 4, the rounded (to 0, 45, 90, 135) angle is stored.
   * At depth = 5, the hysteresis matrix is constructed and stored.
   */
  private double[][][] gradients;

  // Constants for depth-dimension of gradients array.
  private final int GRADIENT_X = 0;
  private final int GRADIENT_Y = 1;
  private final int GRADIENT_STRENGTH = 2;
  private final int GRADIENT_STRENGTH_NORM = 3;
  private final int GRADIENT_DIRECTION = 4;
  private final int HYSTERESIS = 5;

  /**
   * Once the edge detection steps are done, the resulting images are stored in this image map.
   * This image map is the only way to get the resulting images from the detector.
   */
  private Map<Integer, EditableImage> imageMap;

  // Constants and mapping to store images at.
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

  // The different images produced by this algorithm.
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

  /**
   * Run the steps of this Canny Edge Detector the image with the given sigma.
   * @param image The image to run edge detection on.
   * @param sigma The sigma to use for edge detection.
   */
  public CannyEdgeDetector(EditableImage image, double sigma) {
    this.image = image;
    this.sigma = sigma;
    this.imageMap = new HashMap<Integer, EditableImage>();

    // Initialize the gradient values matrix
    gradients = new double[image.getHeight()][image.getWidth()][7];

    // Make sure that everything at the hysteresis depth is initially set to -1.
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        gradients[r][c][HYSTERESIS] = -1;
      }
    }

    // Instantiate each intermediate image
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

    // Add the images to the image map
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

    // Perform edge detection steps
    applyGaussianFilter();
    applyFeatureDetection();
    applyNonMaximumSuppression();
    applyHysteresis(0.1, 0.3);
  }

  /**
   * Applies a Gaussian filter to the original image using a Gaussian kernel with length 2 * sigma.
   */
  private void applyGaussianFilter() {
    // Retrieve the separated single dimension Gaussian kernel for the current sigma.
    double[] kernel = getGaussianKernel();
    int size = (int) (2 * sigma);
    int halfSize = size / 2;
    double val;

    // First, pad the image.
    imageGaussian.padWithZeros(halfSize);

    // For each pixel that isn't padding, convolude the Gaussian kernel with the image.
    // Then update the non-normalized Gaussian image.
    for (int r = 0; r < imageGaussian.getHeight() - kernel.length + 1; r++) {
      for (int c = 0; c < imageGaussian.getWidth() - kernel.length + 1; c++) {
        val = convolude(imageGaussian, kernel, r, c);
        imageGaussian.setGrayscale(c + (halfSize), r + (halfSize), (int) val);
      }
    }

    // Remove the original padding.
    imageGaussian.removePadding();
  }

  /**
   * Returns a single dimensional separated Gaussian kernel based on this sigma.
   * The actual Gaussian is calculated using the Apache Commons Math API.
   * @return A single dimensional separated Gaussian kernel based on this sigma.
   */
  private double[] getGaussianKernel() {
    int size = (int) (2 * sigma);
    double norm = 0;
    // Gaussian object with given mean and sigma.
    Gaussian gaussian = new Gaussian(size / 2, sigma);
    double[] kernel = new double[size];

    // For each index into the kernel, generate a Gaussian value.
    for (int i = 0; i < kernel.length; i++) {
      double val = gaussian.value(i);
      kernel[i] = val;
      norm += val;
    }

    // Normalize the Gaussian values.
    for (int i = 0; i < kernel.length; i++) {
      kernel[i] /= norm;
    }

    return kernel;
  }

  /**
   * Finds gradient directions and strengths over the blurred image.
   */
  private void applyFeatureDetection() {
    // This image is used to convolude over using Sobel's operator.
    EditableImage tmpImage = imageGaussian.copy();

    // The Sobel operator is separable, so we can split the two operators into four single-dimensional operators.
    // Sobel operator in x-direction.
    double[] sobelXX = {1, 2, 1};
    double[] sobelXY = {-1, 0, 1};

    // Sobel operator in y-direction.
    double[] sobelYX = {1, 0, -1};
    double[] sobelYY = {1, 2, 1};

    // Pad the image
    tmpImage.padWithZeros(1);

    // Convolude the image in the x and y-directions to find the gradient strength in the x and y-directions.
    for (int r = 0; r < image.getHeight() - 2; r++) {
      for (int c = 0; c < image.getWidth() - 2; c++) {
        gradients[r][c][GRADIENT_X] = convolude(tmpImage, sobelXX, sobelXY, r, c);
        gradients[r][c][GRADIENT_Y] = convolude(tmpImage, sobelYX, sobelYY, r, c);
      }
    }

    // Remove padding
    tmpImage.removePadding();

    // In order to normalize the gradients, we need to keep track of their min and max values.
    double minX = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;
    double minStrength = Double.MAX_VALUE;
    double maxStrength = Double.MIN_VALUE;

    // Final gradient strength and rounded gradient direction.
    // Also find min and max values for gradients for future normalizations.
    for (int r = 0; r < gradients.length; r++) {
      for (int c = 0; c < gradients[r].length; c++) {
        // Calculate the gradient strength
        gradients[r][c][GRADIENT_STRENGTH] = Math.sqrt(Math.pow(gradients[r][c][GRADIENT_X], 2) + Math.pow(gradients[r][c][GRADIENT_Y], 2));

        // Calculate the rounded gradient direction
        gradients[r][c][GRADIENT_DIRECTION] = roundAngle(Math.atan2(gradients[r][c][GRADIENT_Y], gradients[r][c][GRADIENT_X]));

        // Find min and max gradients for future normalizations.
        minX = gradients[r][c][GRADIENT_X] < minX ? gradients[r][c][GRADIENT_X] : minX;
        maxX = gradients[r][c][GRADIENT_X] > maxX ? gradients[r][c][GRADIENT_X] : maxX;
        minY = gradients[r][c][GRADIENT_Y] < minY ? gradients[r][c][GRADIENT_Y] : minY;
        maxY = gradients[r][c][GRADIENT_Y] > maxY ? gradients[r][c][GRADIENT_Y] : maxY;
        minStrength = gradients[r][c][GRADIENT_STRENGTH] < minStrength ? gradients[r][c][GRADIENT_STRENGTH] : minStrength;
        maxStrength = gradients[r][c][GRADIENT_STRENGTH] > maxStrength ? gradients[r][c][GRADIENT_STRENGTH] : maxStrength;
      }
    }

    // Store normalized (0 - 1) gradient strengths.
    // Update images.
    for (int r = 0; r < gradients.length; r++) {
      for (int c = 0; c < gradients[r].length; c++) {
        // Calculate and store the gradient strength normalized to between 0 and 1.
        gradients[r][c][GRADIENT_STRENGTH_NORM] = (gradients[r][c][GRADIENT_STRENGTH] - minStrength) / (maxStrength - minStrength);

        // Update image of gradient in x-direction
        imageGradientX.setGrayscale(c, r, (int) gradients[r][c][GRADIENT_X]);

        // Update image of gradient in y-direction
        imageGradientY.setGrayscale(c, r, (int) gradients[r][c][GRADIENT_Y]);

        // Update image of gradient in x-direction normalized to between 0 and 255
        imageGradientXNorm.setGrayscale(c, r, (int) ((gradients[r][c][GRADIENT_X] - minX) / (maxX - minX) * 255));

        // Update image of gradient in y-direction normalized to between 0 and 255
        imageGradientYNorm.setGrayscale(c, r, (int) ((gradients[r][c][GRADIENT_Y] - minY) / (maxY - minY) * 255));

        // Update image of gradient strength
        imageGradientStrength.setGrayscale(c, r, (int) gradients[r][c][GRADIENT_STRENGTH]);

        // Update image of gradient strength normalized to between 0 and 255
        imageGradientStrengthNorm.setGrayscale(c, r, (int) ((gradients[r][c][GRADIENT_STRENGTH] - minStrength) / (maxStrength - minStrength) * 255));
      }
    }
  }

  /**
   * Given an angle in radians, returns the angle in degrees rounded to either 0, 45, 90, or 135.
   * The angle at 0 represents the horizontal direction.
   * The angle at 45 represents the diagonal direction from south west to north east.
   * The angle at 90 represents the vertical direction.
   * The angle at 135 represents the diagonal direction from south east to north west.
   * @param radians The radian value to convert and round.
   * @return The angle in degrees rounded to 0, 45, 90, or 135.
   */
  private int roundAngle(double radians) {
    double degrees = Math.toDegrees(radians);

    // Make sure we're using a positive angle
    if (degrees < 0) {
      degrees = 180 + degrees;
    }

    // Perform the rounding
    // Value is close to horizontal
    if ((degrees >= 0 && degrees < 22.5) || (degrees >= 157.5 && degrees <= 180)) {
      degrees = 0;
    }
    // Value is close to diagonal SW to NE
    else if ((degrees >= 22.5 && degrees < 67.5)) {
      degrees = 45;
    }
    // Value is close to vertical
    else if ((degrees >= 67.5 && degrees < 112.5)) {
      degrees = 90;
    }
    // Value is close to diagonal SE to NW
    else if ((degrees >= 112.5 && degrees < 157.5)) {
      degrees = 135;
    }

    return (int) degrees;
  }

  /**
   * Removes pixels with large gradients that are not part of an edge.
   * This has the overall effect of making the edges skinnier and removing noise from the image.
   * This method does two things,it removes non-maximum pixels from the normalized gradient strength image. It also
   * makes sure the corresponding pixels don't find their way into the final image by setting the values in the
   * hysteresis matrix to background.
   */
  private void applyNonMaximumSuppression() {
    imageNonMaximumSuppression = imageGradientStrengthNorm.copy();

    // For each pixel, check it's gradient direction, and then check to see if that pixel should be suppressed by
    // comparing it to it's neighbors which are parallel to the direction of the gradient.
    for (int r = 0; r < gradients.length; r++) {
      for (int c = 0; c < gradients[r].length; c++) {
        switch ((int) gradients[r][c][GRADIENT_DIRECTION]) {
          case 0:
            // Compare with N and S neighbors
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r - 1, c, r + 1, c)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
          case 45:
            // Compare with NW and SE neighbors
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r - 1, c - 1, r + 1, c + 1)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
          case 90:
            // Compare with W and E neighbors
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r, c - 1, r, c + 1)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
          case 135:
            // Compare with NE and SW neighbors
            if (shouldSuppress((int) gradients[r][c][GRADIENT_STRENGTH], r - 1, c + 1, r + 1, c - 1)) {
              imageNonMaximumSuppression.setGrayscale(c, r, 0);
              gradients[r][c][HYSTERESIS] = 0;
            }
            break;
        }
      }
    }
    // Since the reference to imageNonMaximumSuppression was changed, we have to add it back to the map.
    // This was something that I was unaware of and caused quite a bit of pain when trying to debug this step
    // of the process.
    imageMap.put(IMAGE_NON_MAXIMUM_SUPPRESSION, imageNonMaximumSuppression);
  }

  /**
   * Given a gradient strength and two neighbors, decide if the given pixel should be suppressed.
   * Tests whether or not the current pixel's magnitude is greater than its two neighbors.
   * @param gradStrength The gradient strength of the current pixel.
   * @param r1 The row of the first neighbor.
   * @param c1 The column of the first neighbor.
   * @param r2 The row of the second neighbor.
   * @param c2 The column of the second neighbor.
   * @return Whether or not the pixel should be suppressed.
   */
  private boolean shouldSuppress(int gradStrength, int r1, int c1, int r2, int c2) {
    // Check bounds of the first neighbor
    if (r1 < gradients.length && r1 >= 0 && c1 < gradients[r1].length && c1 >= 0) {
      // Check strength against first neighbor
      if (gradStrength < gradients[r1][c1][GRADIENT_STRENGTH]) {
        return true;
      }
    }

    // Check bounds of second neighbor
    if (r2 < gradients.length && r2 >= 0 && c2 < gradients[r2].length && c2 >= 0) {
      // Check strength against second neighbor
      if (gradStrength < gradients[r2][c2][GRADIENT_STRENGTH]) {
        return true;
      }
    }

    // If this point is reached, then the given gradient strength is larger than both of its neighbors.
    return false;
  }

  /**
   * Apply hysteresis over two thresholds, t1 and t2.
   * The thresholds are compared against the normalized gradient strength at each pixel.
   * @param t1 The lower threshold.
   * @param t2 The upper threshold.
   */
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
    // That is, for each pixel, if it an accepted edge pixel, recursively search all 8 neighbors and apply thresholding
    // with t2.
    for(int r = 0; r < gradients.length; r++) {
      for(int c = 0; c < gradients[r].length; c++) {
        if(gradients[r][c][HYSTERESIS] == 255) {
          applyHysteresis(r - 1, c);      // N
          applyHysteresis(r - 1, c + 1);  // NE
          applyHysteresis(r, c + 1);      // E
          applyHysteresis(r + 1, c + 1);  // SE
          applyHysteresis(r + 1, c);      // S
          applyHysteresis(r + 1, c - 1);  // SW
          applyHysteresis(r, c - 1);      // W
          applyHysteresis(r - 1, c - 1);  // NW
        }
      }
    }

    // Set any remaining pixels to black.
    // Also set between thresholds image.
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

  /**
   * Recursively search for and set values that match the threshold t such that t1 > t > t2.
   * This method will only set thresholds at t2 if there is a path from a t2 pixel to a t1 pixel.
   * @param r The row.
   * @param c The column.
   */
  private void applyHysteresis(int r, int c) {
    // Check row bounds
    if(r < 0 || r >= gradients.length) {
      return;
    }

    // Check column bounds
    if(c < 0 || c >= gradients[r].length) {
      return;
    }

    // Are we looking at a pixel that has already been set?
    if(gradients[r][c][HYSTERESIS] != -1) {
      return;
    }

    // Set the value to gray
    gradients[r][c][HYSTERESIS] = 127;

    // Recurs over all 8 neighbors
    applyHysteresis(r - 1, c);      // N
    applyHysteresis(r - 1, c + 1);  // NE
    applyHysteresis(r, c + 1);      // E
    applyHysteresis(r + 1, c + 1);  // SE
    applyHysteresis(r + 1, c);      // S
    applyHysteresis(r + 1, c - 1);  // SW
    applyHysteresis(r, c - 1);      // W
    applyHysteresis(r - 1, c - 1);  // NW
  }

  /**
   * Performs convolution given a single separated kernel which is the same kernel to use in both x and y-directions.
   * @param convoludeImage The image to convolude with.
   * @param kernel The single dimensional separated kernel.
   * @param row The row to convolude at.
   * @param col The column to convolude at.
   * @return The result of convolution at row and col.
   */
  private double convolude(EditableImage convoludeImage, double[] kernel, int row, int col) {
    return this.convolude(convoludeImage, kernel, kernel, row, col);
  }

  /**
   * Performs convolution given two separated kernels at a given row and col.
   * This convolusion is separable. It performs convolusion along the rows, and then convolution along a single
   * column.
   * @param convoludeImage The image to convolude with.
   * @param kernelX The separated kernel in the x-direction.
   * @param kernelY The seperated kernel in the y-direction.
   * @param row The row to convolude at.
   * @param col The column to convolude at.
   * @return The result of convolution at row and col.
   */
  private double convolude(EditableImage convoludeImage, double[] kernelX, double[] kernelY, int row, int col) {
    // Stores the results of convoluding among the rows
    double[] tmp = new double[kernelX.length];
    double sum = 0;

    // First perform convolution along the rows
    for (int r = 0; r < kernelX.length; r++) {
      for (int c = 0; c < kernelX.length; c++) {
        sum += kernelX[c] * convoludeImage.getGrayscale(c + col, r + row);
      }
      tmp[r] = sum;
      sum = 0;
    }

    // Convolude along the single column.
    for (int i = 0; i < kernelY.length; i++) {
      sum += kernelY[i] * tmp[i];
    }

    return sum;
  }

  /**
   * Return one of the images stored during processing.
   * @param IMAGE_TYPE The image types given by this class.
   * @return The EditableImage stored at the IMAGE_TYPE location.
   */
  public EditableImage getImage(final int IMAGE_TYPE) {
    return imageMap.get(IMAGE_TYPE);
  }
}
