package net.sf.taverna.t2.activities.wsdl.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.security.profiles.WSSecurityProfile;
import net.sf.taverna.t2.security.profiles.ui.WSSecurityProfileChooser;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WSDLActivityConfigureAction extends ActivityConfigurationAction<WSDLActivity> {

	private final Frame owner;
	private static Logger logger = Logger
			.getLogger(WSDLActivityConfigureAction.class);

	public WSDLActivityConfigureAction(WSDLActivity activity,Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		WSDLActivityConfigurationBean bean = getActivity().getConfiguration();
			
		WSSecurityProfileChooser wsSecurityProfileChooser = new WSSecurityProfileChooser(owner);
		if (wsSecurityProfileChooser.isInitialised()) {
			wsSecurityProfileChooser.setVisible(true);
		}
		
		WSSecurityProfile wsSecurityProfile = wsSecurityProfileChooser.getWSSecurityProfile();
		String profileString;
		if (wsSecurityProfile != null) { // user did not cancel
			profileString = wsSecurityProfile.getWSSecurityProfileString();
			logger.info("WSSecurityProfile string read as:"+profileString);
			bean.setSecurityProfileString(profileString);
			configureActivity(bean);
		}
		
	}

}
