package edu.achriste.ui;

import edu.achriste.image.CannyEdgeDetector;
import edu.achriste.image.EditableImage;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
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
  private JTextField out;

  private EditableImage originalImage;

  /**
   * Sets up the user interface and display the original originalImage.
   */
  private CannyEdgeDetectorUI() {
    this.setLayout(new BorderLayout());
    JPanel optionsPanel = new JPanel(new BorderLayout());

    out = new JTextField();
    out.setEditable(false);

    originalImage = new EditableImage(new File("img/board.tif"));
    imagePanel = new ImagePanel(originalImage.getImage());
    imagePanel.resize();

    /*
    Options for list.
   */
    String[] OPTIONS = {
            "Original Image",
            "Smoothed Image sigma = 1.5",
            "Smoothed Image sigma = 2.5",
            "Smoothed Image sigma = 3.5",
            "Feature Detection sigma = 1.5",
            "Feature Detection sigma = 2.5",
            "Feature Detection sigma = 3.5",
    };
    optionsList = new JList<String>(OPTIONS);
    optionsList.addListSelectionListener(this);
    optionsList.setSelectedIndex(0);

    optionsPanel.add(optionsList, BorderLayout.CENTER);
    optionsPanel.add(out, BorderLayout.SOUTH);
    optionsPanel.setPreferredSize(new Dimension(250, imagePanel.getHeight()));

    this.add(imagePanel, BorderLayout.CENTER);
    this.add(optionsPanel, BorderLayout.WEST);
  }



  /**
   * Updates the originalImage and message depending on which menu option is chosen.
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    CannyEdgeDetector edgeDetector;
    if (!e.getValueIsAdjusting()) {
      int i = optionsList.getSelectedIndex();

      switch (i) {
        case 0: // Original originalImage
          imagePanel.setBufferedImage(originalImage.getImage());
          break;
        case 1:
          EditableImage smooth15 = originalImage.copy();
          edgeDetector = new CannyEdgeDetector(smooth15, 1.5);
          edgeDetector.applyGaussianFilter();
          imagePanel.setBufferedImage(smooth15.getImage());
          break;
        case 2:
          EditableImage smooth25 = originalImage.copy();
          edgeDetector = new CannyEdgeDetector(smooth25, 2.5);
          edgeDetector.applyGaussianFilter();
          imagePanel.setBufferedImage(smooth25.getImage());
          break;
        case 3:
          EditableImage smooth35 = originalImage.copy();
          edgeDetector = new CannyEdgeDetector(smooth35, 3.5);
          edgeDetector.applyGaussianFilter();
          imagePanel.setBufferedImage(smooth35.getImage());
          break;
        case 4:
          EditableImage feature15 = originalImage.copy();
          edgeDetector = new CannyEdgeDetector(feature15, 1.5);
          edgeDetector.applyGaussianFilter();
          edgeDetector.applyFeatureDetection();
          imagePanel.setBufferedImage(feature15.getImage());
          break;
        case 5:
          EditableImage feature25 = originalImage.copy();
          edgeDetector = new CannyEdgeDetector(feature25, 2.5);
          edgeDetector.applyGaussianFilter();
          edgeDetector.applyFeatureDetection();
          imagePanel.setBufferedImage(feature25.getImage());
          break;
        case 6:
          EditableImage feature35 = originalImage.copy();
          edgeDetector = new CannyEdgeDetector(feature35, 3.5);
          edgeDetector.applyGaussianFilter();
          edgeDetector.applyFeatureDetection();
          imagePanel.setBufferedImage(feature35.getImage());
          break;
      }
    }
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

