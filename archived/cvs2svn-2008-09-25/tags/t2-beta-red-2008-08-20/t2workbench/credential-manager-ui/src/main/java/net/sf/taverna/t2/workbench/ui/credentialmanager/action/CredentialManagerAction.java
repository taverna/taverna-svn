package net.sf.taverna.t2.workbench.ui.credentialmanager.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.security.credentialmanager.ui.CredentialManagerGUI;

@SuppressWarnings("serial")
public class CredentialManagerAction extends AbstractAction {

	private static ImageIcon ICON = new ImageIcon(CredentialManagerAction.class.getResource("/cred_manager16x16.png"));
	
	public CredentialManagerAction() {
		super("Credential Manager",ICON);
	}
	
	public void actionPerformed(ActionEvent e) {
		CredentialManagerGUI cmGUI = new CredentialManagerGUI();
		if (cmGUI.isInitialised()) {
			cmGUI.setVisible(true);
		}
	}

	

}
