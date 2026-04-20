package ca.mcgill.ecse420.a3;
import java.util.Random;

public class MatrixVectorBenchmark {

    private static final Random rand = new Random();

    public static double[][] generateRandomMatrix(int n) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = rand.nextDouble() * 20 - 10;
            }
        }
        return matrix;
    }

    public static double[] generateRandomVector(int n) {
        double[] vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = rand.nextDouble() * 20 - 10;
        }
        return vector;
    }

    public static boolean compareVectors(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            return false;
        }

        for (int i = 0; i < v1.length; i++) {
            if (Math.abs(v1[i] - v2[i]) > 1e-6) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        int[] sizes = {100, 500, 1000, 2000, 4000};
        int[] thresholds = {1, 5, 10, 20, 50, 100, 200, 500, 1000};

        for (int n : sizes) {
            System.out.println("\nMatrix size: " + n + " x " + n);

            double[][] matrix = generateRandomMatrix(n);
            double[] vector = generateRandomVector(n);

            long start = System.nanoTime();
            double[] sequentialResult = SequentialMatrixVector.multiply(matrix, vector);
            long end = System.nanoTime();

            double seqTime = (end - start) / 1e6;
            System.out.printf("Sequential Time: %.3f ms%n", seqTime);

            double bestParTime = Double.MAX_VALUE;
            int bestThreshold = -1;

            for (int threshold : thresholds) {
                ParallelMatrixVector pmv = new ParallelMatrixVector(threshold);

                start = System.nanoTime();
                double[] parallelResult = pmv.multiply(matrix, vector);
                end = System.nanoTime();

                double parTime = (end - start) / 1e6;
                double speedup = seqTime / parTime;
                int tasks = pmv.getTaskCount(n);

                System.out.printf(
                    "Threshold = %4d | Tasks = %4d | Parallel Time = %8.3f ms | Speedup = %.3f | Correct = %s%n",
                    threshold, tasks, parTime, speedup, compareVectors(sequentialResult, parallelResult)
                );

                if (parTime < bestParTime) {
                    bestParTime = parTime;
                    bestThreshold = threshold;
                }

                pmv.shutdown();
            }

            System.out.printf("Best threshold for n = %d: %d%n", n, bestThreshold);
            System.out.printf("Best parallel time: %.3f ms%n", bestParTime);
            System.out.printf("Best speedup: %.3f%n", seqTime / bestParTime);
        }
    }
}