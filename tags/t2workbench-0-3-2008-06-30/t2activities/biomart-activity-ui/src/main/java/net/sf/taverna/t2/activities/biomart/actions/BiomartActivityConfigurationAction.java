package net.sf.taverna.t2.activities.biomart.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.Element;

public class BiomartActivityConfigurationAction extends ActivityConfigurationAction<BiomartActivity> {

	private static final long serialVersionUID = 3782223454010961660L;

	public BiomartActivityConfigurationAction(BiomartActivity activity) {
		super(activity);
	}

	@SuppressWarnings("serial")
	public void actionPerformed(ActionEvent action) {
		final BiomartConfigurationPanel configurationPanel = new BiomartConfigurationPanel(getActivity().getConfiguration());
		final JDialog dialog = new JDialog();
		
		Action okAction = new AbstractAction("OK") {

			public void actionPerformed(ActionEvent arg0) {
				Element query = configurationPanel.getQuery();
				BiomartActivityConfigurationBean bean = new BiomartActivityConfigurationBean();
				bean.setQuery(query);
				try {
					getActivity().configure(bean);
				} catch (ActivityConfigurationException e) {
					e.printStackTrace();
				}
				dialog.setVisible(false);
			}
			
		};
		Action cancelAction = new AbstractAction("Cancel") {

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
			
		};
		
		configurationPanel.setOkAction(okAction);
		configurationPanel.setCancelAction(cancelAction);
		
		dialog.getContentPane().add(configurationPanel);
		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);
	} 

}
