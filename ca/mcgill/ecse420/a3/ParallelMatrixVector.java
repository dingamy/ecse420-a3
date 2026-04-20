package ca.mcgill.ecse420.a3;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelMatrixVector {
    private final ExecutorService exec;
    private final int threshold; // number of rows per task

    public ParallelMatrixVector(int threshold) {
        this.exec = Executors.newCachedThreadPool();
        this.threshold = threshold;
    }

    public double[] multiply(double[][] A, double[] x) throws Exception {
        int n = A.length;

        double[] result = new double[n];
        List<Future<?>> futures = new ArrayList<>();

        for (int start = 0; start < n; start += threshold) {
            int end = Math.min(start + threshold, n);
            futures.add(exec.submit(new MultiplyTask(A, x, result, start, end)));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        return result;
    }

    private static class MultiplyTask implements Runnable {
        private final double[][] matrix;
        private final double[] vector;
        private final double[] result;
        private final int start;
        private final int end;

        MultiplyTask(double[][] matrix, double[] vector, double[] result, int start, int end) {
            this.matrix = matrix;
            this.vector = vector;
            this.result = result;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                double sum = 0.0;
                for (int j = 0; j < vector.length; j++) {
                    sum += matrix[i][j] * vector[j];
                }
                result[i] = sum;
            }
        }
    }

    public int getTaskCount(int n) {
        return (n + threshold - 1) / threshold;
    }

    public void shutdown() {
        exec.shutdown();
    }
}