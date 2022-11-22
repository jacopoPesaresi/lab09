package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

//import java.util.List;
//import java.util.stream.DoubleStream;

/**
 * 
 */
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int usedThread;

    /**
     * 
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
         *            the list to sum
         * @param startRow
         *            the initial position for this worker
         * @param finRow
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startRow, final int amountWork) {
            super();
            this.matrix = matrix;
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
        /*****
        //final int size = list.size() % usedThread + list.size() / usedThread;
        final int amountWork = matrix.length / usedThread + 1;
        double tmp = 0.0;
        double[] setOfWork = new double[usedThread];

        MyWorker actTh;// = new Agent();
        //final AgentController aContr = new AgentController(myAgent);
        //new Thread(new AgentController(myAgent)).start();
        //new Thread(myAgent).start();

        for (int i=0; i < usedThread; i++) {
            actTh = new MyWorker(matrix, i, amountWork);
            actTh.start();
            setOfWork[i] = actTh.getMyResult();
        }
        *****/

        /*
         * Build a stream of workers
         */

        /*
        return DoubleStream
                .iterate(0, start -> start + (matrix[0].length * amountWork))
                .limit(usedThread)
                .mapToObj(start -> new MyWorker(matrix, 0, amountWork))
                // Start them
                .peek(Thread::start)
                // Join them
                .peek(MultiThreadedSumMatrix::joinUninterruptibly)
                 // Get their result and sum
                .mapToDouble(MyWorker::getMyResult)
                .sum();
        */

        //////return tmp;
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
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
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

    /**
     * 
     * @param target
     */
    /*
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (InterruptedException e) {
                e.printStackTrace(); //NOPMD
            }
        }
    }
    */
}
