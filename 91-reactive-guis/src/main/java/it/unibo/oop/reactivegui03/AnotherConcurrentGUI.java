package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 3L;
    private static final double WIDTH_PERC = 0.3;
    private static final double HEIGHT_PERC = 0.125;
    private final JLabel display = new JLabel();
    private static final JButton UP = new JButton("up");
    private static final JButton DOWN = new JButton("down");
    private static final JButton STOP = new JButton("stop");

    /**
     * 
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(UP);
        panel.add(DOWN);
        panel.add(STOP);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it.
         */
        final Agent myAgent = new Agent();
        new Thread(myAgent).start();
        /*
         * Register a listener that stops it
         */
        UP.addActionListener((e) -> myAgent.setIncrement(true));
        DOWN.addActionListener((e) -> myAgent.setIncrement(false));
        STOP.addActionListener((e) -> {
            myAgent.stopCounting();
            AnotherConcurrentGUI.unsetAllButtons();
        });
    }

    /**
     * Little class' util to unset the "enalble" of all buttons.
     */
    public static void unsetAllButtons() {
        UP.setEnabled(false);
        DOWN.setEnabled(false);
        STOP.setEnabled(false);
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        private static final int SLEEP_TIME = 100;
        private volatile boolean stopGate;
        private volatile boolean increment = true;
        private int counter;

        @Override
        public void run() {
            final AgentStopper stopper = new AgentStopper();
            new Thread(stopper).start();
            //final long passedTime = System.currentTimeMillis();
            while (!this.stopGate && !stopper.aStop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (increment) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();  //NOPMD
                }
            }
            //System.out.println("\n### " + (System.currentTimeMillis() - passedTime) + "###\n");  //NOPMD
            AnotherConcurrentGUI.unsetAllButtons();
        }

        /**
         * @param status boolean value to set the way that counter 
         * value grow
         */
        public void setIncrement(final boolean status) {
            this.increment = status;
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stopGate = true;
        }
    }

    private static class AgentStopper implements Runnable {
        private static final long TOWAIT = 400L;
        private static final long TOSLEEP = 10_000L / TOWAIT;
        private volatile boolean aStop;
        private long index;

        @Override
        public void run() {
            try {
                while (!aStop) {
                    index++;
                    checkDeath();
                    Thread.sleep(TOSLEEP);
                }
            } catch (InterruptedException e) {
                e.printStackTrace(); //NOPMD it's an esercice
            }
        }

        /**
         * 
         */
        public void checkDeath() {
            if (index == TOWAIT) {
                aStop = true;
            }
        }
    }
}
