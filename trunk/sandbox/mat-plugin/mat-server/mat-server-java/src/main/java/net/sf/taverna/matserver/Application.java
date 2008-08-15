package net.sf.taverna.matserver;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author petarj
 */
public class Application extends JFrame {

    private static final long serialVersionUID = 7434716524883186229L;
    private static MatServer server;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    
    public Application()
    {
        super("mat-server");
        init();
        server=new MatServer();
        server.setPort(server.DEFAULT_PORT);
    }
    
    private void init()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cp=getContentPane();
        cp.setLayout(new FlowLayout()); //TODO: make this grid layout
        
        portField=new JTextField();
        portField.setColumns(10);
        add(portField);
        
        startButton=new JButton("Start");
        startButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int port=Integer.parseInt(portField.getText());
                server.setPort(port);
                try {
                    server.start();
                } catch (Exception ex) {
                    Logger.getLogger(Application.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        });
        add(startButton);
        
        stopButton=new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    server.stop();
                } catch (Exception ex) {
                    Logger.getLogger(Application.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        });
        add(stopButton);
        pack();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Application app=new Application();
                app.setVisible(true);
            }
        });
    }
}
