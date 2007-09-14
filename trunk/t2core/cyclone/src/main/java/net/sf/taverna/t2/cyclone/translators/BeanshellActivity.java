package net.sf.taverna.t2.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
//FIXME: this doesn't belong in this package. It should be moved to a separate module
public class BeanshellActivity extends AbstractActivity<BeanshellConfigurationType> {

	@Override
	public void configure(BeanshellConfigurationType conf)
			throws ActivityConfigurationException {
		// TODO Auto-generated method stub	
	}

	@Override
	public BeanshellConfigurationType getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
