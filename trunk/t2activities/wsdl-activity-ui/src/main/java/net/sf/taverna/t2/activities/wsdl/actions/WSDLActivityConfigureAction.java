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
		
		WSDLConfigureDialogue configureDialogue=new WSDLConfigureDialogue(owner);
		configureDialogue.setVisible(true);
		
		WSSecurityProfile securityProfile = configureDialogue.getWSSecurityProfile();
		if (securityProfile!=null) {
			String profileString = securityProfile.getWSSecurityProfileString();
			logger.info("WSSecurityProfile String read as:"+profileString);
			bean.setSecurityProfileString(profileString);
			configureActivity(bean);
		}
	}
	
	class WSDLConfigureDialogue extends JDialog {
		
		private WSSecurityProfileChooser securityProfileChooser;

		public WSDLConfigureDialogue(Frame owner) {
			super(owner,true);
			setLayout(new BorderLayout());
			securityProfileChooser = new WSSecurityProfileChooser();
			add(securityProfileChooser.getContentPane(),BorderLayout.CENTER);
			
			JPanel buttons = new JPanel();
			buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton okButton = new JButton("OK");
			JButton cancelButton = new JButton("Cancel");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					WSDLConfigureDialogue.this.setVisible(false);
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					WSDLConfigureDialogue.this.setVisible(false);
				}
			});
			
			buttons.add(cancelButton);
			buttons.add(okButton);
			add(buttons,BorderLayout.SOUTH);
			pack();
		}
		
		public WSSecurityProfile getWSSecurityProfile() {
			return securityProfileChooser.getWSSecurityProfile();
		}
		
	}

}
