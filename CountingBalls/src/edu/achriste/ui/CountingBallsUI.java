package edu.achriste.ui;

import edu.achriste.processing.ConnectedComponents;
import edu.achriste.processing.DiskSE;
import edu.achriste.processing.Erosion;
import edu.achriste.processing.Otsu;
import edu.achriste.utils.ImageUtils;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * User interface to display images and allow switching between images.
 *
 * @author Anthony Christe
 */
public class CountingBallsUI extends JPanel implements ListSelectionListener {
  private static final long serialVersionUID = 7202334436935353130L;

  // Indices to store images and messages at
  private final int ORIGINAL_IMAGE = 0;
  private final int BINARY_IMAGE = 1;
  private final int ERODE_4_IMAGE = 2;
  private final int ERODE_5_IMAGE = 3;
  private final int ERODE_6_IMAGE = 4;
  private final int COMPONENT_4_IMAGE = 5;
  private final int COMPONENT_5_IMAGE = 6;
  private final int COMPONENT_6_IMAGE = 7;

  /**
   * Stores images from all processing actions.
   */
  private ArrayList<BufferedImage> images;

  /**
   * Stores message associated with each image.
   */
  private ArrayList<String> messages;

  // User interface components
  private ImagePanel imagePanel;
  private JList<String> optionsList;
  private JTextField out;

  /**
   * Sets up the user interface and display the original image.
   */
  private CountingBallsUI() {
    messages = new ArrayList<String>();
    this.setLayout(new BorderLayout());
    JPanel optionsPanel = new JPanel(new BorderLayout());

    out = new JTextField();
    out.setEditable(false);

    images = performImageCalculations();

    imagePanel = new ImagePanel(ImageUtils.copyBufferedImage(images.get(ORIGINAL_IMAGE)));
    imagePanel.resize();

    /*
    Options for list.
   */
    String[] OPTIONS = {
            "Original Image",
            "Binary Image",
            "Disk-SE 4",
            "Disk-SE 5",
            "Disk-SE 6",
            "Connected Components 4",
            "Connected Components 5",
            "Connected Components 6"};
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
   * Performs all image calculations and returns the resulting images.
   * @return A list of images after each calculation.
   */
  private ArrayList<BufferedImage> performImageCalculations() {
    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

    // Original image
    images.add(ORIGINAL_IMAGE, ImageUtils.readBufferedImage(out));
    messages.add(ORIGINAL_IMAGE, "Original Image");

    // Perform Otsu's Method
    int threshold = Otsu.otsusMethod(images.get(ORIGINAL_IMAGE));
    images.add(BINARY_IMAGE, ImageUtils.makeBinary(images.get(ORIGINAL_IMAGE), threshold));
    messages.add(BINARY_IMAGE, String.format("Binary Image - threshold = %d", threshold));

    // Perform erosion
    images.add(ERODE_4_IMAGE, Erosion.erode(images.get(BINARY_IMAGE), new DiskSE(4)));
    images.add(ERODE_5_IMAGE, Erosion.erode(images.get(BINARY_IMAGE), new DiskSE(5)));
    images.add(ERODE_6_IMAGE, Erosion.erode(images.get(BINARY_IMAGE), new DiskSE(6)));
    messages.add(ERODE_4_IMAGE, "Erosion - DiskSE 4");
    messages.add(ERODE_5_IMAGE, "Erosion - DiskSE 5");
    messages.add(ERODE_6_IMAGE, "Erosion - DiskSE 6");

    // Perform connected component labeling
    ConnectedComponents components4 = new ConnectedComponents(images.get(ERODE_4_IMAGE));
    ConnectedComponents components5 = new ConnectedComponents(images.get(ERODE_5_IMAGE));
    ConnectedComponents components6 = new ConnectedComponents(images.get(ERODE_6_IMAGE));
    images.add(COMPONENT_4_IMAGE, components4.getBufferedImage());
    images.add(COMPONENT_5_IMAGE, components5.getBufferedImage());
    images.add(COMPONENT_6_IMAGE, components6.getBufferedImage());
    messages.add(COMPONENT_4_IMAGE, String.format("Connected Component 4 - %d found", components4.getComponentCount()));
    messages.add(COMPONENT_5_IMAGE, String.format("Connected Component 5 - %d found", components5.getComponentCount()));
    messages.add(COMPONENT_6_IMAGE, String.format("Connected Component 6 - %d found", components6.getComponentCount()));

    return images;
  }

  /**
   * Updates the image and message depending on which menu option is chosen.
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      int i = optionsList.getSelectedIndex();

      switch (i) {
        case 0: // Original image
          imagePanel.setBufferedImage(images.get(ORIGINAL_IMAGE));
          out.setText(messages.get(ORIGINAL_IMAGE));
          break;
        case 1: // Binary image
          imagePanel.setBufferedImage(images.get(BINARY_IMAGE));
          out.setText(messages.get(BINARY_IMAGE));
          break;
        case 2: // Eroded with disk se 4
          imagePanel.setBufferedImage(images.get(ERODE_4_IMAGE));
          out.setText(messages.get(ERODE_4_IMAGE));
          break;
        case 3: // Eroded with disk se 5
          imagePanel.setBufferedImage(images.get(ERODE_5_IMAGE));
          out.setText(messages.get(ERODE_5_IMAGE));
          break;
        case 4: // Eroded with disk se 6
          imagePanel.setBufferedImage(images.get(ERODE_6_IMAGE));
          out.setText(messages.get(ERODE_6_IMAGE));
          break;
        case 5: // Connected component labeling with disk se 4
          imagePanel.setBufferedImage(images.get(COMPONENT_4_IMAGE));
          out.setText(messages.get(COMPONENT_4_IMAGE));
          break;
        case 6: // Connected component labeling with disk se 5
          imagePanel.setBufferedImage(images.get(COMPONENT_5_IMAGE));
          out.setText(messages.get(COMPONENT_5_IMAGE));
          break;
        case 7: // Connected component labeling with disk se 6
          imagePanel.setBufferedImage(images.get(COMPONENT_6_IMAGE));
          out.setText(messages.get(COMPONENT_6_IMAGE));
          break;
      }
    }
  }

  /**
   * Sets up the JFrame and loads an instance of the user interface.
   */
  private static void init() {
    JFrame frame = new JFrame("Counting Balls - Anthony Christe");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new CountingBallsUI());
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

