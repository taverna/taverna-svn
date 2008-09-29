/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

@SuppressWarnings("serial")
public class BiomobyActivityConfigurationAction
		extends
		ActivityConfigurationAction<BiomobyActivity, BiomobyActivityConfigurationBean> {

	private final Frame owner;

	public BiomobyActivityConfigurationAction(BiomobyActivity activity,
			Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent arg0) {
		JOptionPane.showMessageDialog(owner, "Not yet implemented!!!");
	}

}
