package net.sf.taverna.t2.security.profiles.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


public class WSSecurityProfileChooserLauncher extends JFrame {
	
	private static final long serialVersionUID = 2079805060170251148L;
	
	public WSSecurityProfileChooserLauncher(){
		
		JPanel jpLaunch = new JPanel();
		jpLaunch.setPreferredSize(new Dimension (300, 120));
		
		JLabel jlLaunch = new JLabel("T2: WSS Profile Chooser");
		
		JButton jbLaunch = new JButton("WSS Profile Chooser");
		jbLaunch.setToolTipText("Launches WSS Profile Chooser");
		jbLaunch.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				WSSecurityProfileChooser wssChooser = new WSSecurityProfileChooser();
				if (wssChooser.isInitialised()){
					wssChooser.setVisible(true);
				}
			}
		});
		
		jpLaunch.add(jlLaunch);
		jpLaunch.add(jbLaunch);

		getContentPane().add(jpLaunch,BorderLayout.CENTER);
		
        // Handle application close
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    
		pack();

        // Centre the frame in the centre of the desktop
        setLocationRelativeTo(null);
        
        // Set the frame's title
        setTitle("WS Security Profile Launcher");
        
        setVisible(true);
		
	}
	
	
    /**
     * Runnable to create and show the Credential Manager Launcher's GUI.
     */
    private static class CreateAndShowGui
        implements Runnable
    {

        /**
         * Create and show the launcher GUI.
         */
        public void run()
        {
            new WSSecurityProfileChooserLauncher();
        }
    }
    
    
    /**
     * Launcher for the Credential Manager GUI.
     */
    public static void main(String[] args)
    {
        // Create and show GUI on the event handler thread
        SwingUtilities.invokeLater(new CreateAndShowGui());
    }

}
