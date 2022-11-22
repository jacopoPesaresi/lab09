package it.unibo.oop.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 2L;
    private static final double WIDTH_PERC = 0.3;
    private static final double HEIGHT_PERC = 0.125;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /**
     * 
     */
    public ConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Agent agent = new Agent();
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
         * Create the counter agent and start it. 
         */
        new Thread(agent).start();
        /*
         * Handlers
         */
        up.addActionListener((e) -> agent.setIncrement(true));
        down.addActionListener((e) -> agent.setIncrement(false));
        stop.addActionListener((e) -> {
            agent.stopCounting();
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
        private volatile boolean stop;
        private volatile boolean increment = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
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
        }

        /**
         * External switch to change the sense of increase.
         * @param status
         *  pass:
         *      "true" to increase the value;
         *      "false" to decrease the value.
         */
        public void setIncrement(final boolean status) {
            this.increment = status;
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
    }
}
