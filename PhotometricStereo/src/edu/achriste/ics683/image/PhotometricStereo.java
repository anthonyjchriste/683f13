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
  private double[][] heightMap;

  private static final int NORMAL_X = 0;
  private static final int NORMAL_Y = 1;
  private static final int NORMAL_Z = 2;

  public PhotometricStereo(EditableImage[] editableImages, String sourcesPath) {
    this.matrices = new Matrix[editableImages.length];

    for(int i = 0; i < editableImages.length; i++) {
      this.matrices[i] = new Matrix(normalizeIntensities(editableImages[i]));
    }

    this.sourceMatrix = getSourceMatrix(sourcesPath);
    this.getSurfaceDescription();
    this.getHeightMap();
    this.writeImageFiles();
  }

  private void getSurfaceDescription() {
    gradients = new Matrix[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    albedo = new double[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    normals = new Matrix[matrices[0].getRowDimension()][matrices[0].getColumnDimension()];
    Matrix intensityMatrix = null;

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
    for(int r = 0; r < 4; r++) {
      for(int c = 0; c < 3; c++) {
        matrix.set(r, c, (sourceMatrix.get(r, c) * intensityMatrix.get(r, 0)));
      }
    }

    return matrix;
  }

  private double normalize(double minVal, double maxVal, double maxNorm, double val) {
    return  ((val - minVal) / (maxVal - minVal)) * maxNorm;
  }

  private double min(double val, double min) {
    return val < min ? val : min;
  }

  private double max(double val, double max) {
    return val > max ? val : max;
  }

  private void getHeightMap() {
    double sumCol = 0;
    double sumRow = 0;
    heightMap = new double[albedo.length][albedo[0].length];

    for(int r = 0; r < heightMap.length; r++) {
      sumRow += normals[r][0].get(NORMAL_Y, 0);
      for(int c = 0; c < heightMap[r].length; c++) {
        sumCol += normals[r][c].get(NORMAL_X, 0);
        heightMap[r][c] = sumRow + sumCol;
      }
      sumCol = 0;
    }
  }

  private void writeImageFiles() {
    EditableImage albedos = new EditableImage(albedo[0].length, albedo.length);
    EditableImage normalsX = new EditableImage(albedo[0].length, albedo.length);
    EditableImage normalsY = new EditableImage(albedo[0].length, albedo.length);
    EditableImage normalsZ = new EditableImage(albedo[0].length, albedo.length);
    EditableImage heightMapImage = new EditableImage(albedo[0].length, albedo.length);

    double minNormalX = Double.MAX_VALUE;
    double maxNormalX = Double.MIN_VALUE;
    double minNormalY = Double.MAX_VALUE;
    double maxNormalY = Double.MIN_VALUE;
    double minNormalZ = Double.MAX_VALUE;
    double maxNormalZ = Double.MIN_VALUE;
    double minHeight = Double.MAX_VALUE;
    double maxHeight = Double.MIN_VALUE;

    for(int r = 0; r < albedo.length; r++) {
      for(int c = 0; c < albedo[r].length; c++) {
        albedos.setGrayscale(c, r, (int) (albedo[r][c] * 255));

        minNormalX = min(normals[r][c].get(NORMAL_X, 0), minNormalX);
        maxNormalX = max(normals[r][c].get(NORMAL_X, 0), maxNormalX);

        minNormalY = min(normals[r][c].get(NORMAL_Y, 0), minNormalY);
        maxNormalY = max(normals[r][c].get(NORMAL_Y, 0), maxNormalY);

        minNormalZ = min(normals[r][c].get(NORMAL_Z, 0), minNormalZ);
        maxNormalZ = max(normals[r][c].get(NORMAL_Z, 0), maxNormalZ);

        minHeight = min(heightMap[r][c], minHeight);
        maxHeight = max(heightMap[r][c], maxHeight);
      }
    }

    for(int r = 0; r < albedo.length; r++) {
      for(int c = 0; c < albedo[r].length; c++) {
        normalsX.setGrayscale(c, r, (int) normalize(minNormalX, maxNormalX, 255, normals[r][c].get(NORMAL_X, 0)));
        normalsY.setGrayscale(c, r, (int) normalize(minNormalY, maxNormalY, 255, normals[r][c].get(NORMAL_Y, 0)));
        normalsZ.setGrayscale(c, r, (int) normalize(minNormalZ, maxNormalZ, 255, normals[r][c].get(NORMAL_Z, 0)));
        heightMapImage.setGrayscale(c, r, (int) normalize(minHeight, maxHeight, 255, heightMap[r][c]));
      }
    }

    albedos.writeImage("img/out/albedo.png");
    normalsX.writeImage("img/out/normals-x.png");
    normalsY.writeImage("img/out/normals-y.png");
    normalsZ.writeImage("img/out/normals-z.png");
    heightMapImage.writeImage("img/out/height-map.png");
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
