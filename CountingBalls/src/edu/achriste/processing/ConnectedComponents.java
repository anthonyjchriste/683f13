package edu.achriste.processing;

import edu.achriste.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Represents a connected component matrix.
 *
 * @author Anthony Christe
 */
public class ConnectedComponents {
  /**
   * Initially represent all foreground pixels as -1.
   */
  private final int FOREGROUND = -1;
  private final int BACKGROUND = 0;
  private int[][] matrix;
  private int label = 1;
  private BufferedImage image;

  /**
   * Initialize this matrix with the given image.
   * @param image The image to find connected componented over.
   */
  public ConnectedComponents(BufferedImage image) {
    this.image = image;
    this.matrix = new int[image.getHeight()][image.getWidth()];
    this.initMatrix();
    this.scan();
  }

  /**
   * Returns the number of connected components in this image.
   * @return The number of connected components in this image.
   */
  public int getComponentCount() {
    return label;
  }

  /**
   * Make sure all foreground values are set.
   */
  private void initMatrix() {
    for (int r = 0; r < image.getHeight(); r++) {
      for (int c = 0; c < image.getWidth(); c++) {
        if (ImageUtils.isForeground(image, c, r)) {
          matrix[r][c] = FOREGROUND;
        }
      }
    }
  }

  /**
   * Scans each pixel and recursively visits neighbors if a non-labeled foreground pixel is found.
   */
  private void scan() {
    for (int r = 0; r < matrix.length; r++) {
      for (int c = 0; c < matrix[r].length; c++) {
        if (matrix[r][c] == FOREGROUND) {
          setNeighbors(r, c, label++);
        }
      }
    }
  }

  /**
   * Recursively visits all neighbors in a 4-neighborhood and marks them with the current label.
   * @param r The pixel row.
   * @param c The column row.
   * @param label The label to assign to the pixels.
   */
  private void setNeighbors(int r, int c, int label) {
    if (r < 0 || r >= matrix.length) {
      return;
    }
    if (c < 0 || c >= matrix[r].length) {
      return;
    }
    if (matrix[r][c] == BACKGROUND) {
      return;
    }
    if (matrix[r][c] == FOREGROUND) {
      matrix[r][c] = label;
      setNeighbors(r - 1, c, label);
      setNeighbors(r, c + 1, label);
      setNeighbors(r + 1, c, label);
      setNeighbors(r, c - 1, label);
    }
  }

  /**
   * Generates a List of 4096 unique colors.
   * @return A list of 4096 unique colors.
   */
  private ArrayList<Integer> getColors() {
    ArrayList<Integer> colors = new ArrayList<Integer>(16 * 16 * 16);
    for (int r = 0; r < 256; r += 16) {
      for (int g = 0; g < 256; g += 16) {
        for (int b = 0; b < 256; b += 16) {
          colors.add(ImageUtils.getRgb(r, g, b));
        }
      }
    }
    return colors;
  }

  /**
   * Randomly assigns a unique color to each connected component and returns those values in a map.
   * @return A map that maps each label to a unique random color.
   */
  private Map<Integer, Integer> getLabelsToColors() {
    Random random = new Random();
    ArrayList<Integer> colors = getColors();
    Map<Integer, Integer> labelsToColors = new HashMap<Integer, Integer>();

    labelsToColors.put(0, ImageUtils.rgbFromGrayscale(255));
    for (int i = 1; i <= label; i++) {
      labelsToColors.put(i, colors.remove(random.nextInt(colors.size())));
    }
    return labelsToColors;
  }

  /**
   * Build a buffered image from the connected component matrix and use the random colors obtained earlier.
   * @return A buffered image from the connected component matrix.
   */
  public BufferedImage getBufferedImage() {
    BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Map<Integer, Integer> labelsToColors = getLabelsToColors();

    for (int r = 0; r < matrix.length; r++) {
      for (int c = 0; c < matrix[r].length; c++) {
        bufferedImage.setRGB(c, r, labelsToColors.get(matrix[r][c]));
      }
    }
    return bufferedImage;
  }
}
