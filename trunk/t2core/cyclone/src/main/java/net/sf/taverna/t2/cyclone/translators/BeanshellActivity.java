package net.sf.taverna.t2.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;
//FIXME: this doesn't belong in this package. It should be moved to a separate module
/**
 * <p>
 * A semi-dummy Activity relating to Beanshell functionality. Eventually this class
 * will not exist as part of t2core but will be part of a Beanshell activity artifact in its own right.
 * </p>
 * 
 * @author Stuart Owen
 */
public class BeanshellActivity extends AbstractActivity<BeanshellActivityConfigurationBean> {

	private BeanshellActivityConfigurationBean configurationBean;
	
	@Override
	protected ActivityPortBuilder getPortBuilder() {
		return ActivityPortBuilderImpl.getInstance();
	}

	@Override
	public void configure(BeanshellActivityConfigurationBean configurationBeans)
			throws ActivityConfigurationException {
		this.configurationBean=configurationBeans;
		configurePorts(configurationBeans);	
		
	}

	@Override
	public BeanshellActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}
}
