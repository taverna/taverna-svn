package net.sf.taverna.t2.security.test.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.security.credentialmanager.ui.CredentialManagerGUI;
import net.sf.taverna.t2.security.profiles.WSSecurityProfile;
import net.sf.taverna.t2.security.profiles.ui.WSSecurityProfileChooser;


public class Launcher extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private final ImageIcon launchCMIcon = new ImageIcon(getClass().getResource(
	"/images/cred_manager.png"));
	
	public Launcher(){
		
		/* Credential Manager Launcher */
		JPanel jpLaunchCM = new JPanel();
		//jpLaunchCM.setPreferredSize(new Dimension (300, 120));
		
		JLabel jlLaunchCM = new JLabel("Credential Manager");
		
		JButton jbLaunchCM = new JButton();
		jbLaunchCM.setIcon(launchCMIcon);
		jbLaunchCM.setToolTipText("Launches Credential Manager");
		jbLaunchCM.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				CredentialManagerGUI cmGUI = new CredentialManagerGUI();
				if (cmGUI.isInitialised()){
					cmGUI.setVisible(true);
				}
			}
		});
		
		jpLaunchCM.add(jlLaunchCM);
		jpLaunchCM.add(jbLaunchCM);
		getContentPane().add(jpLaunchCM,BorderLayout.NORTH);
		
		/* Profile Chooser Launcher */
		JPanel jpLaunchPC = new JPanel();
		//jpLaunchCM.setPreferredSize(new Dimension (300, 120));
		
		JLabel jlLaunchPC = new JLabel("WS Security settings");
		
		JButton jbLaunchPC = new JButton();
		jbLaunchPC.setIcon(launchCMIcon);
		jbLaunchPC.setToolTipText("Launches WSS Profile Chooser");
		jbLaunchPC.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				WSSecurityProfileChooser wssPC = new WSSecurityProfileChooser(Launcher.this);
				if (wssPC.isInitialised()) {
					wssPC.setVisible(true);
				}

				WSSecurityProfile wssProfile = wssPC.getWSSecurityProfile();
				if (wssProfile != null) // user did not cancel
					System.err.println(wssProfile.getWSSecurityProfileString());
			}
		});
		
		jpLaunchPC.add(jlLaunchPC);
		jpLaunchPC.add(jbLaunchPC);
		getContentPane().add(jpLaunchPC,BorderLayout.SOUTH);
		
        // Handle application close
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    
		pack();

        // Centre the frame in the centre of the desktop
        setLocationRelativeTo(null);
        
        // Set the frame's title
        setTitle("T2 Launcher");
        
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
            new Launcher();
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
