package ca.mcgill.ecse420.a3;
public class SequentialMatrixVector {
    public static double[] multiply(double[][] A, double[] x) {
        int rows = A.length;
        int cols = A[0].length;

        if (x.length != cols) {
            throw new IllegalArgumentException("Matrix columns must match vector size.");
        }

        double[] result = new double[rows];

        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += A[i][j] * x[j];
            }
            result[i] = sum;
        }

        return result;
    }
}