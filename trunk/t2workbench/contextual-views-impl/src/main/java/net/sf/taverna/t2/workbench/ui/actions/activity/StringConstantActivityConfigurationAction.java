package net.sf.taverna.t2.workbench.ui.actions.activity;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class StringConstantActivityConfigurationAction extends
		ActivityConfigurationAction<StringConstantActivity> {

	private static final long serialVersionUID = 2518716617809186972L;

	public StringConstantActivityConfigurationAction(StringConstantActivity activity) {
		super(activity);
	}

	public void actionPerformed(ActionEvent e) {
		StringConstantConfigurationBean bean = getActivity().getConfiguration();
		String value = getActivity().getConfiguration().getValue();
		String newValue = JOptionPane.showInputDialog("New string value",value);
		if (newValue!=null) {
			bean.setValue(newValue);
			
			try {
				getActivity().configure(bean);
			} catch (ActivityConfigurationException e1) {
				//FIXME: handle configuration exception
			}
		}
	}

}
