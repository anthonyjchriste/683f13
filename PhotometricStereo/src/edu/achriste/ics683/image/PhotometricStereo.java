package edu.achriste.ics683.image;

import Jama.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    this.getSurfaceDescription();
    this.writeImageFiles();
  }

  private void getSurfaceDescription() {
    gradients = new Matrix[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    albedo = new double[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    normals = new Matrix[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    Matrix intensityMatrix;

    for(int r = 0; r < matrices[0].getRowDimension(); r++) {
      for(int c = 0; c < matrices[0].getColumnDimension(); c++) {
        intensityMatrix = getIntensityMatrix(r, c);
        //gradients[r][c] = handleShadows(intensityMatrix).inverse().times(intensityMatrix);
        gradients[r][c] = sourceMatrix.inverse().times(intensityMatrix);
        albedo[r][c] = MatrixUtils.magnitude(gradients[r][c]);
        normals[r][c] = gradients[r][c].times(1 / albedo[r][c]);
      }
    }
  }

  private Matrix handleShadows(Matrix intensityMatrix) {
    Matrix matrix = new Matrix(sourceMatrix.getRowDimension(), sourceMatrix.getColumnDimension());
    for(int r = 0; r < intensityMatrix.getRowDimension(); r++) {
      for(int c = 0; c < sourceMatrix.getColumnDimension(); c++) {
        matrix.set(r, c, (sourceMatrix.get(r, c) * intensityMatrix.get(r, 0)));
      }
    }

    return matrix;
  }

  private void writeImageFiles() {
    EditableImage albedos = new EditableImage(albedo[0].length, albedo.length);
    EditableImage normalsA = new EditableImage(albedo[0].length, albedo.length);
    EditableImage normalsB = new EditableImage(albedo[0].length, albedo.length);
    EditableImage normalsC = new EditableImage(albedo[0].length, albedo.length);
    double minNormal = Double.MAX_VALUE;
    double maxNormal = Double.MIN_VALUE;

    for(int r = 0; r < albedo.length; r++) {
      for(int c = 0; c < albedo[r].length; c++) {
        albedos.setGrayscale(c, r, (int) (albedo[r][c] * 255));
        //normalsA.setGrayscale(c, r, (int) normals[r][c].get(0, 0));
        //normalsB.setGrayscale(c, r, (int) normals[r][c].get(1, 0));
        //normalsC.setGrayscale(c, r, (int) normals[r][c].get(2, 0));

        minNormal = normals[r][c].get(0, 0) < minNormal ? normals[r][c].get(0, 0) : minNormal;
        minNormal = normals[r][c].get(1, 0) < minNormal ? normals[r][c].get(0, 0) : minNormal;
        minNormal = normals[r][c].get(2, 0) < minNormal ? normals[r][c].get(0, 0) : minNormal;
        maxNormal = normals[r][c].get(0, 0) > maxNormal ? normals[r][c].get(0, 0) : maxNormal;
        maxNormal = normals[r][c].get(1, 0) > maxNormal ? normals[r][c].get(0, 0) : maxNormal;
        maxNormal = normals[r][c].get(2, 0) > maxNormal ? normals[r][c].get(0, 0) : maxNormal;
      }
    }
    System.out.format("%f %f\n", minNormal, maxNormal);
    for(int r = 0; r < albedo.length; r++) {
      for(int c = 0; c < albedo[r].length; c++) {
        normalsA.setGrayscale(c, r, (int) ((normals[r][c].get(0, 0) - minNormal) / (maxNormal - minNormal) * 255));
        normalsB.setGrayscale(c, r, (int) ((normals[r][c].get(1, 0) - minNormal) / (maxNormal - minNormal) * 255));
        normalsC.setGrayscale(c, r, (int) ((normals[r][c].get(2, 0) - minNormal) / (maxNormal - minNormal) * 255));
        //System.out.println(normalsA.getGrayscale(c, r));
      }
    }

    albedos.writeImage("img/out/albedo.png");
    normalsA.writeImage("img/out/normals-a.png");
    normalsB.writeImage("img/out/normals-b.png");
    normalsC.writeImage("img/out/normals-c.png");
  }

  private Matrix getIntensityMatrix(int r, int c) {
    Matrix intensityMatrix = new Matrix(matrices.length, 1);

    for(int i = 0; i < matrices.length; i++) {
      intensityMatrix.set(i, 0, matrices[i].get(r, c));
    }

    return intensityMatrix;
  }

  private Matrix getSourceMatrix(String filePath) {
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

    //return sourceMatrix.inverse();
    return sourceMatrix;
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

  }
}
