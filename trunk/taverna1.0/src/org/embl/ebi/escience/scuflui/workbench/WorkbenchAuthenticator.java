/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * Subclass of Authenticator which throws up the appropriate
 * dialog in the workbench when a request is made which requires
 * a username / password of some kind. Hopefully should resolve
 * some of the issues with authenticating proxies.
 * @author Tom Oinn
 */
public final class WorkbenchAuthenticator extends Authenticator {
    
    // Map of host+port+prompt -> PasswordAuthentication (yuck)
    // used to remember credentials when the user has asked for
    // this behaviour.
    private final Map credentials = new HashMap();
    
    private JFrame hostFrame = null;
    
    public PasswordAuthentication getPasswordAuthentication() {
	System.out.println("Request for authentication");
	String host = getRequestingHost();
	if (host == null) {
	    host = "Unknown host";
	}
	String port = getRequestingPort()+"";
	String prompt = getRequestingPrompt();
	PasswordAuthentication pa = (PasswordAuthentication)credentials.get(host+port+prompt);
	if (pa != null) {
	    // Return cached result
	    return pa;
	}
	PasswordDialog pd = new PasswordDialog(this.hostFrame, true, "Password for "+host+", "+prompt+" required.");
	if (pd.saveCredentials) {
	    credentials.put(host+port+prompt,pd.result);
	}
	return pd.result;
    }
    
    public WorkbenchAuthenticator(JFrame hostFrame) {
	super();
	this.hostFrame = hostFrame;
    }
    
    public Map getStoredCredentials() {
	return this.credentials;
    }

}
class PasswordDialog extends JDialog {
    
    PasswordAuthentication result = null;
    JTextField user;
    JPasswordField password;
    boolean saveCredentials = false;
    
    public PasswordDialog(JFrame frame, boolean modal, String myMessage) {
	super(frame, modal);
	JPanel myPanel = new JPanel();
	getContentPane().add(myPanel);
	myPanel.setLayout(new BorderLayout());
	myPanel.add(new JLabel(myMessage), BorderLayout.NORTH);
	JPanel inputPanel = new JPanel();
	myPanel.add(inputPanel, BorderLayout.CENTER);
	inputPanel.setLayout(new GridLayout(2,2));
	user = new JTextField();
	password = new JPasswordField();
	inputPanel.add(new JLabel("Username"));
	inputPanel.add(user);
	inputPanel.add(new JLabel("Password"));
	inputPanel.add(password);
	JPanel buttonPanel = new JPanel();
	myPanel.add(buttonPanel, BorderLayout.SOUTH);
	JButton okay = new JButton("Okay");
	JButton cancel = new JButton("Cancel");
	buttonPanel.setLayout(new BorderLayout());
	buttonPanel.add(cancel, BorderLayout.WEST);
	buttonPanel.add(okay, BorderLayout.EAST);
	okay.setMaximumSize(new Dimension(80,20));
	cancel.setMaximumSize(new Dimension(80,20));
	okay.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    result = new PasswordAuthentication(user.getText(), password.getPassword());
		    setVisible(false);
		}
	    });
	cancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    result = null;
		    setVisible(false);
		}
	    });
	JPanel savePasswordPanel = new JPanel();
	myPanel.add(savePasswordPanel, BorderLayout.NORTH);
	JCheckBox savePassword = new JCheckBox("Save password",false);
	savePasswordPanel.add(Box.createHorizontalGlue());
	savePasswordPanel.add(savePassword);
	savePassword.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.DESELECTED) {
			saveCredentials = false;
		    }
		    else {
			saveCredentials = true;
		    }
		}
	    });
	pack();
	setLocationRelativeTo(frame);
	setVisible(true);
	
    }
}
