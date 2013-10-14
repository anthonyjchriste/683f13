package edu.achriste.ui;

import edu.achriste.image.CannyEdgeDetector;
import edu.achriste.image.EditableImage;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

/**
 * User interface to display images and allow switching between images.
 *
 * @author Anthony Christe
 */
public class CannyEdgeDetectorUI extends JPanel implements ListSelectionListener {
  private static final long serialVersionUID = 7202334436935353130L;

  // User interface components
  private ImagePanel imagePanel;
  private JList<String> optionsList;

  private EditableImage originalImage;
  private CannyEdgeDetector cannyEdgeDetector15;
  private CannyEdgeDetector cannyEdgeDetector25;
  private CannyEdgeDetector cannyEdgeDetector35;

  /**
   * Sets up the user interface and display the original originalImage.
   */
  private CannyEdgeDetectorUI() {
    this.setLayout(new BorderLayout());
    JPanel optionsPanel = new JPanel(new BorderLayout());

    originalImage = new EditableImage(new File("img/board.tif"));
    imagePanel = new ImagePanel(originalImage.getImage());
    imagePanel.resize();

    /*
    Options for list.
   */
    String[] OPTIONS = {
            "Original Image",
            "------ sigma = 1.5 ------",
            "1.5 Gaussian",
            "1.5 Gradient X Normalized",
            "1.5 Gradient Y Normalized",
            "1.5 Gradient Strength Normalized",
            "1.5 Non-Maximum Suppression",
            "1.5 High Threshold",
            "1.5 Between Thresholds",
            "1.5 Hysteresis",
            "------ sigma = 2.5 ------",
            "2.5 Gaussian",
            "2.5 Gradient X Normalized",
            "2.5 Gradient Y Normalized",
            "2.5 Gradient Strength Normalized",
            "2.5 Non-Maximum Suppression",
            "2.5 High Threshold",
            "2.5 Between Thresholds",
            "2.5 Hysteresis",
            "------ signma = 3.5 ------",
            "3.5 Gaussian",
            "3.5 Gradient X Normalized",
            "3.5 Gradient Y Normalized",
            "3.5 Gradient Strength Normalized",
            "3.5 Non-Maximum Suppression",
            "3.5 High Threshold",
            "3.5 Between Thresholds",
            "3.5 Hysteresis"
    };
    optionsList = new JList<String>(OPTIONS);
    optionsList.addListSelectionListener(this);
    optionsList.setSelectedIndex(0);

    optionsPanel.add(optionsList, BorderLayout.CENTER);
    optionsPanel.setPreferredSize(new Dimension(250, imagePanel.getHeight()));

    this.add(imagePanel, BorderLayout.CENTER);
    this.add(optionsPanel, BorderLayout.WEST);

    // Do computations
    cannyEdgeDetector15 = new CannyEdgeDetector(originalImage, 1.5);
    cannyEdgeDetector25 = new CannyEdgeDetector(originalImage, 2.5);
    cannyEdgeDetector35 = new CannyEdgeDetector(originalImage, 3.5);
    writeImages();
  }

  /**
   * Updates the originalImage and message depending on which menu option is chosen.
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      int i = optionsList.getSelectedIndex();

      switch (i) {
        case 0:
          imagePanel.setBufferedImage(originalImage.getImage());
          break;
        case 1: break; // Spacer
        case 2:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GAUSSIAN).getImage());
          break;
        case 3:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GRADIENT_X_NORM).getImage());
          break;
        case 4:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GRADIENT_Y_NORM).getImage());
          break;
        case 5:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GRADIENT_STRENGTH_NORM).getImage());
          break;
        case 6:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_NON_MAXIMUM_SUPPRESSION).getImage());
          break;
        case 7:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_HIGH_THRESHOLD).getImage());
          break;
        case 8:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_BETWEEN_THRESHOLD).getImage());
          break;
        case 9:
          imagePanel.setBufferedImage(cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_HYSTERESIS).getImage());
          break;
        case 10: break; // Spacer
        case 11:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GAUSSIAN).getImage());
          break;
        case 12:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GRADIENT_X_NORM).getImage());
          break;
        case 13:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GRADIENT_Y_NORM).getImage());
          break;
        case 14:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GRADIENT_STRENGTH_NORM).getImage());
          break;
        case 15:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_NON_MAXIMUM_SUPPRESSION).getImage());
          break;
        case 16:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_HIGH_THRESHOLD).getImage());
          break;
        case 17:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_BETWEEN_THRESHOLD).getImage());
          break;
        case 18:
          imagePanel.setBufferedImage(cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_HYSTERESIS).getImage());
          break;
        case 19: break; // Spacer
        case 20:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GAUSSIAN).getImage());
          break;
        case 21:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GRADIENT_X_NORM).getImage());
          break;
        case 22:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GRADIENT_Y_NORM).getImage());
          break;
        case 23:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GRADIENT_STRENGTH_NORM).getImage());
          break;
        case 24:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_NON_MAXIMUM_SUPPRESSION).getImage());
          break;
        case 25:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_HIGH_THRESHOLD).getImage());
          break;
        case 26:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_BETWEEN_THRESHOLD).getImage());
          break;
        case 27:
          imagePanel.setBufferedImage(cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_HYSTERESIS).getImage());
          break;
      }
    }
  }

  /**
   * Write all computed images to a file.
   */
  private void writeImages() {
    originalImage.writeImage("images/original.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GAUSSIAN).writeImage("images/1.5/0_gaussian.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GRADIENT_X_NORM).writeImage("images/1.5/1_gradient_x_norm.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GRADIENT_Y_NORM).writeImage("images/1.5/2_gradient_y_norm.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_GRADIENT_STRENGTH_NORM).writeImage("images/1.5/3_gradient_strength_norm.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_NON_MAXIMUM_SUPPRESSION).writeImage("images/1.5/4_non_maximum_suppression.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_HIGH_THRESHOLD).writeImage("images/1.5/5_high_threshold.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_BETWEEN_THRESHOLD).writeImage("images/1.5/6_between_thresholds.tiff");
    cannyEdgeDetector15.getImage(CannyEdgeDetector.IMAGE_HYSTERESIS).writeImage("images/1.5/7_final_hysteresis.tiff");

    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GAUSSIAN).writeImage("images/2.5/0_gaussian.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GRADIENT_X_NORM).writeImage("images/2.5/1_gradient_x_norm.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GRADIENT_Y_NORM).writeImage("images/2.5/2_gradient_y_norm.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_GRADIENT_STRENGTH_NORM).writeImage("images/2.5/3_gradient_strength_norm.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_NON_MAXIMUM_SUPPRESSION).writeImage("images/2.5/4_non_maximum_suppression.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_HIGH_THRESHOLD).writeImage("images/2.5/5_high_threshold.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_BETWEEN_THRESHOLD).writeImage("images/2.5/6_between_thresholds.tiff");
    cannyEdgeDetector25.getImage(CannyEdgeDetector.IMAGE_HYSTERESIS).writeImage("images/2.5/7_final_hysteresis.tiff");

    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GAUSSIAN).writeImage("images/3.5/0_gaussian.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GRADIENT_X_NORM).writeImage("images/3.5/1_gradient_x_norm.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GRADIENT_Y_NORM).writeImage("images/3.5/2_gradient_y_norm.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_GRADIENT_STRENGTH_NORM).writeImage("images/3.5/3_gradient_strength_norm.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_NON_MAXIMUM_SUPPRESSION).writeImage("images/3.5/4_non_maximum_suppression.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_HIGH_THRESHOLD).writeImage("images/3.5/5_high_threshold.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_BETWEEN_THRESHOLD).writeImage("images/3.5/6_between_thresholds.tiff");
    cannyEdgeDetector35.getImage(CannyEdgeDetector.IMAGE_HYSTERESIS).writeImage("images/3.5/7_final_hysteresis.tiff");
  }

  /**
   * Sets up the JFrame and loads an instance of the user interface.
   */
  private static void init() {
    JFrame frame = new JFrame("Canny Edge Detector - Anthony Christe");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new CannyEdgeDetectorUI());
    JFrame.setDefaultLookAndFeelDecorated(true);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Entry point into the program.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        init();
      }
    });
  }
}

