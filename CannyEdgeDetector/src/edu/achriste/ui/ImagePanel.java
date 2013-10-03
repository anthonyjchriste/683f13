package edu.achriste.ui;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Represents a JPanel that can display BufferedImage's.
 *
 * @author Anthony Christe
 */
class ImagePanel extends JPanel {
  private static final long serialVersionUID = -1731282137392767887L;
  private BufferedImage bufferedImage;

  public ImagePanel(BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
  }

  /**
   * Draws the image on the panel.
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(bufferedImage, 0, 0, null);
  }

  /**
   * Resize the JPanel to fit the width/height of the image.
   */
  void resize() {
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();
    this.setPreferredSize(new Dimension(width, height));
  }

  /**
   * Returns the image stored in this JPanel.
   * @return The image stored in this JPanel.
   */
  protected BufferedImage getBufferedImage() {
    return this.bufferedImage;
  }

  /**
   * Sets the image for this panel to display.
   * @param image The image to display.
   */
  void setBufferedImage(BufferedImage image) {
    this.bufferedImage = image;
    this.resize();
    this.repaint();
  }
}
