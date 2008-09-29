package net.sf.taverna.matserver;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author petarj
 */
public class Application extends JFrame {

    private static final long serialVersionUID = 7434716524883186229L;
    private static MatServer server;
    private JButton startButton;
    private JButton stopButton;
    private JButton statusButton;
    private JTextArea logArea;
    private boolean running = false;

    public Application() {
        super("mat-server");
        init();
        server = new MatServer();
        server.setPort(MatServer.DEFAULT_PORT);
    }

    private void init() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout()); //TODO: make this grid layout

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = GridBagConstraints.REMAINDER;

        logArea = new JTextArea(16, 32);
        logArea.setEditable(false);
        cp.add(logArea, constraints);

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    server.start();
                    running = true;
                    logArea.append(">Server started.\n");
                } catch (Exception ex) {
                    Logger.getLogger(Application.class.getName()).
                            log(Level.SEVERE, null, ex);
                    running = false;
                    logArea.append("Server startup failed.\n");
                }
            }
        });

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;

        add(startButton, constraints);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    server.stop();
                    running = false;
                    logArea.append(">Server stopped.\n");
                } catch (Exception ex) {
                    Logger.getLogger(Application.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        });

        constraints.gridy = 1;
        add(stopButton, constraints);

        statusButton = new JButton("Status");
        statusButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (running) {
                    logArea.append(">Server is running.\n");
                } else {
                    logArea.append(">Server is inactive.\n");
                }
            }
        });

        constraints.gridy = 2;
        add(statusButton, constraints);

        pack();
        setResizable(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Application app = new Application();
                app.setVisible(true);
            }
        });
    }
}
