package net.sf.taverna.sf.cyclone.translators;

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.processor.service.AbstractService;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceConfigurationException;
//FIXME: this doesn't belong in this package. It should be moved to a separate module
public class BeanshellService extends AbstractService<BeanshellConfigurationType> {

	@Override
	public void configure(BeanshellConfigurationType conf)
			throws ServiceConfigurationException {
		// TODO Auto-generated method stub	
	}

	@Override
	public BeanshellConfigurationType getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
