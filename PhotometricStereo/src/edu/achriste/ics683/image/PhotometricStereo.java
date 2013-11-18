package edu.achriste.ics683.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import Jama.Matrix;

public class PhotometricStereo {
  private Matrix[] matrices;
  private Matrix sourceMatrix;
  private Matrix[][] gradients;
  private Matrix[][] normals;
  private double[][] albedo;

  public PhotometricStereo(EditableImage[] editableImages, String sourcesPath) {
    this.matrices = new Matrix[editableImages.length];

    for(int i = 0; i < editableImages.length; i++) {
      this.matrices[i] = new Matrix(normalizeIntensities(editableImages[i]));
    }

    this.sourceMatrix = getSourceMatrix(sourcesPath);
  }

  public void getSurfaceDescription() {
    gradients = new Matrix[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    albedo = new double[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    normals = new Matrix[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];

    for(int r = 0; r < matrices[0].getRowDimension(); r++) {
      for(int c = 0; c < matrices[0].getColumnDimension(); c++) {
        gradients[r][c] = this.sourceMatrix.times(getIntensityMatrix(r, c));
        albedo[r][c] = MatrixUtils.magnitude(gradients[r][c]);
        normals[r][c] = gradients[r][c].times(1 / albedo[r][c]);
      }
    }

    // TODO: mult by image intensities
    // TODO: Write to file
  }

  public Matrix getIntensityMatrix(int r, int c) {
    Matrix intensityMatrix = new Matrix(matrices.length, 1);

    for(int i = 0; i < matrices.length; i++) {
      intensityMatrix.set(i, 0, matrices[i].get(r, c));
    }

    return intensityMatrix;
  }

  public Matrix getSourceMatrix(String filePath) {
    Scanner in = null;
    List<String> lines = new ArrayList<String>();

    try {
      in = new Scanner(new File(filePath));
      while(in.hasNextLine()) {
        lines.add(in.nextLine());
      }
    } catch (FileNotFoundException e) {
      System.err.format("File %s not found");
    } finally {
      if(in != null) {
        in.close();
      }
    }

    Matrix sourceMatrix = new Matrix(lines.size(), 3);
    String[] splitLine;

    for(int r = 0; r < lines.size(); r++) {
      splitLine = lines.get(r).split(",");
      for(int c = 0; c < 3; c++) {
        sourceMatrix.set(r, c, Double.parseDouble(splitLine[c]));
      }
    }

    return sourceMatrix.inverse();
  }

  private double[][] normalizeIntensities(EditableImage image) {
    double[][] normalizedIntensities = new double[image.getHeight()][image.getWidth()];

    for(int r = 0; r < image.getHeight(); r++) {
      for(int c = 0; c < image.getWidth(); c++) {
        normalizedIntensities[r][c] = image.getGrayscale(c, r) / 255.0;
      }
    }

    return normalizedIntensities;
  }


  // TODO: Proper usage and arg checking
  public static void main(String... args) {
    String sourcesFile = args[0];
    EditableImage[] images = new EditableImage[args.length - 1];

    for(int i = 1; i < args.length; i++) {
      images[i - 1] = new EditableImage(new File(args[i]));
    }

    PhotometricStereo photometricStereo = new PhotometricStereo(images, sourcesFile);

    photometricStereo.getSurfaceDescription();
  }
}
