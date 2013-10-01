package edu.achriste.processing;

import edu.achriste.utils.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * This class contains methods for performing Otsu's Method and manipulating histograms.
 *
 * @author Anthony Christe
 */
public class Otsu {

  /**
   * Perform Otsu's method to find a threshold over a given grayscale image.
   * @param image The image to perform Otsu's Method on.
   * @return The threshold value from Otsu's Method on the given image.
   */
  public static int otsusMethod(BufferedImage image) {
    double[] histogram = getHistogram(image);

    // Group probability less than threshold
    double q1;

    // Group probability greater than threshold
    double q2;

    // Group means less than threshold
    double u1;

    // Group means greater than threshold
    double u2;

    // Maximum between class variance
    double max = 0;

    // Index (threshold) that corresponds to the max between class variance
    int maxIndex = -1;

    double tmp;

    // For each threshold, calculate the group probabilities, group means, and between class variance.
    // Update the max between class variance and index if needed.
    for (int t = 0; t < histogram.length; t++) {
      q1 = getQ1(t, histogram);
      q2 = getQ2(t, histogram);
      u1 = getU1(t, q1, histogram);
      u2 = getU2(t, q2, histogram);
      tmp = getWithinVariance(q1, u1, u2);

      if (tmp > max) {
        max = tmp;
        maxIndex = t;
      }
    }
    return maxIndex;
  }

  /**
   * Returns a histogram representing the probability of a grayscale value appearing in the image.
   * @param image The image to generate a histogram for.
   * @return A single dimensional array where each index value corresponds to grayscale value, and the value stored at
   *         that index is the probability of that grayscale value appearing in the image.
   */
  private static double[] getHistogram(BufferedImage image) {
    double[] histogram = new double[256];
    long numPixels = image.getHeight() * image.getWidth();

    // Calculates the total number of pixels at each grayscale value
    for (int r = 0; r < image.getWidth(); r++) {
      for (int c = 0; c < image.getHeight(); c++) {
        histogram[ImageUtils.grayscaleFromRgb(image.getRGB(c, r))]++;
      }
    }

    // Divides the number of each pixels at each grayscale value by the total number of pixels to find find the
    // probability.
    for (int i = 0; i < histogram.length; i++) {
      histogram[i] = (histogram[i] / numPixels);
    }
    return histogram;
  }

  /**
   * Calculates the group probabilities for values less than the threshold.
   * @param threshold The current threshold.
   * @param histogram The histogram of pixel probabilities.
   * @return The group probability for values less than the current threshold.
   */
  private static double getQ1(int threshold, double[] histogram) {
    double sum = 0;
    for (int i = 0; i < threshold; i++) {
      sum += histogram[i];
    }
    return sum;
  }

  /**
   * Calculates the group probabilities for values greater than the threshold.
   * @param threshold The current threshold.
   * @param histogram The histogram of pixel probabilities.
   * @return The group probability for values greater than the current threshold.
   */
  private static double getQ2(int threshold, double[] histogram) {
    double sum = 0;
    for (int i = threshold; i < histogram.length; i++) {
      sum += histogram[i];
    }
    return sum;
  }

  /**
   * Calculates the group mean for values less than the threshold.
   * @param threshold The current threshold.
   * @param q1 The current group probability for values less than the threshold.
   * @param histogram The histogram of pixel probabilities.
   * @return The group mean for values less than the threshold.
   */
  private static double getU1(int threshold, double q1, double[] histogram) {
    double mean = 0;
    for (int i = 0; i < threshold; i++) {
      mean += (i * histogram[i]) / q1;
    }
    return mean;
  }

  /**
   * Calculates the group mean for values greater than the threshold.
   * @param threshold The current threshold.
   * @param q2 The current group probability for values greater than the threshold.
   * @param histogram The histogram of pixel probabilities.
   * @return The group mean for values greater than the threshold.
   */
  private static double getU2(int threshold, double q2, double[] histogram) {
    double mean = 0;
    for (int i = threshold; i < histogram.length; i++) {
      mean += (i * histogram[i]) / q2;
    }
    return mean;
  }

  /**
   * Calculates the between (or within) group variance.
   * @param q1 The group probability for values less than the current threshold.
   * @param u1 The group means for values less than the current threshold.
   * @param u2 The group means for values greater than the current threshold.
   * @return The between group variance for the current threshold.
   */
  private static double getWithinVariance(double q1, double u1, double u2) {
    return q1 * (1 - q1) * Math.pow(u1 - u2, 2);
  }
}
