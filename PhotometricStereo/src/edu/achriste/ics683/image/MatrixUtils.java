package edu.achriste.ics683.image;


import Jama.Matrix;

public class MatrixUtils {
  public static String toString(Matrix matrix) {
    StringBuffer buffer = new StringBuffer();
    for(int r = 0; r < matrix.getRowDimension(); r++) {
      for(int c = 0; c < matrix.getColumnDimension(); c++) {
        buffer.append(matrix.get(r, c) + " ");
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }

  public static double magnitude(Matrix vector) {
    double squareSum = 0;
    for(int r = 0; r < vector.getRowDimension(); r++) {
      for(int c = 0; c < vector.getColumnDimension(); c++) {
        squareSum += (vector.get(r, c) * vector.get(r, c));
      }
    }
    return Math.sqrt(squareSum);
  }

}
