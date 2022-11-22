package it.unibo.oop.reactivegui03;


import java.awt.Dimension;
import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
//import java.util.concurrent.TimeUnit;

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
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

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
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent myAgent = new Agent();
        //final AgentController aContr = new AgentController(myAgent);
        //new Thread(new AgentController(myAgent)).start();
        new Thread(myAgent).start();
        //final AgentStopper myStopper = new AgentStopper(myAgent);
        //new Thread(myStopper).start();
        /*
         * Register a listener that stops it
         */
        up.addActionListener((e) -> myAgent.setIncrement(true));
        down.addActionListener((e) -> myAgent.setIncrement(false));
        stop.addActionListener((e) -> {
            myAgent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        });
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
            final long a = System.currentTimeMillis();
            while (!this.stopGate && !stopper.aStop) { //&& !stopper.aStop) {
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
            System.out.println("\n\n### " + (System.currentTimeMillis() - a) + "###\n\n"); //NOPMD
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        }

        /**
         * 
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

    /*
    private static class AgentController implements Runnable {
        private final Agent toStop;
        private static final long TOWAIT = 10_000L;

        AgentController(final Agent toControl) {
            this.toStop = toControl;
            new Thread(toControl).start();
        }
        @Override
        public void run() {
            try {
                Thread.sleep(TOWAIT);
                toStop.stopCounting();
            } catch (InterruptedException e) {
                e.printStackTrace(); //NOPMD it's an esercice
            }
        }
    }
    */
    private static class AgentStopper implements Runnable {
        private static final long TOWAIT = 100L;
        private boolean aStop;
        private long index;

        @Override
        public void run() {
            try {
                while (!aStop) {
                    index++;
                    checkDeth();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace(); //NOPMD it's an esercice
            }
        }

        /**
         * 
         */
        public void checkDeth() {
            if (index == TOWAIT) {
                aStop = true;
            }
        }
    }
}
