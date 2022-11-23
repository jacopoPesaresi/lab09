package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int usedThread;

    /**
     * @param nThreads
     */
    public MultiThreadedSumMatrix(final int nThreads) {
        this.usedThread = nThreads;
    }

    /**
     * 
     */
    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startRow;
        private final int amountWork;
        private double res;

        /**
         * Build a new worker.
         * 
         * @param matrix
         *            the matrix of value to sum
         * @param startRow
         *            the initial position for this worker
         * @param amountWork
         *            the amount of rows that this worker have to sum
         */
        Worker(final double[][] matrix, final int startRow, final int amountWork) {
            super();
            this.matrix = matrix.clone();
            this.startRow = startRow;
            this.amountWork = amountWork;
        }

        @Override
        public void run() {
            System.out.println("Working from row " + startRow + " to position " + (startRow + amountWork)); //NOPMD
            for (int i = startRow; 
                startRow + amountWork > matrix.length ? i < matrix.length : i < startRow + amountWork;
                i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    res += matrix[i][j];
                }
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public double getMyResult() {
            return this.res;
        }

    }

    @Override
    public double sum(final double[][] matrix) {
        final int amountWork = matrix.length / usedThread + 1;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(usedThread);
        for (int start = 0; start < matrix.length; start += amountWork) {
            workers.add(new Worker(matrix, start, amountWork));
        }
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. 
         */
        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getMyResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }
}
