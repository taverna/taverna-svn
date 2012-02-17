package net.sf.taverna.t2.activities.interaction.config;

import java.awt.GridLayout;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;



@SuppressWarnings("serial")
public class InteractionActivityConfigurationPanel
		extends
		ActivityConfigurationPanel<InteractionActivity, 
        InteractionActivityConfigurationBean> {

	private InteractionActivity activity;
	private InteractionActivityConfigurationBean configBean;

	public InteractionActivityConfigurationPanel(InteractionActivity activity) {
		this.activity = activity;
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 2));

		// Populate fields from activity configuration bean
		refreshConfiguration();
	}

	/**
	 * Check that user values in UI are valid
	 */
	@Override
	public boolean checkValues() {

		// All valid, return true
		return true;
	}

	/**
	 * Return configuration bean generated from user interface last time
	 * noteConfiguration() was called.
	 */
	@Override
	public InteractionActivityConfigurationBean getConfiguration() {
		// Should already have been made by noteConfiguration()
		return configBean;
	}

	/**
	 * Check if the user has changed the configuration from the original
	 */
	@Override
	public boolean isConfigurationChanged() {
return false;
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
/*		configBean = new InteractionActivityConfigurationBean();
		
		// FIXME: Update bean fields from your UI elements
		configBean.setExampleString(fieldString.getText());
		configBean.setExampleUri(URI.create(fieldURI.getText()));*/
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
/*		configBean = activity.getConfiguration();
		
		// FIXME: Update UI elements from your bean fields
		fieldString.setText(configBean.getExampleString());
		fieldURI.setText(configBean.getExampleUri().toASCIIString());*/
	}
}
