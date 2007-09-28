package net.sf.taverna.t2.activities.beanshell.translator;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Beanshell Processors to a Taverna 2 Beanshell Activity
 * 
 * @see ActivityTranslator
 * @author Stuart Owen
 */
public class BeanshellActivityTranslator extends AbstractActivityTranslator<BeanshellActivityConfigurationBean> {

	@Override
	protected Activity<BeanshellActivityConfigurationBean> createUnconfiguredActivity() {
		return new BeanshellActivity();
	}

	@Override
	protected BeanshellActivityConfigurationBean createConfigType(
			Processor processor) {
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		
		//TODO: use introspection to avoid direct version dependency
		//bean.setScript(((BeanshellProcessor)processor).getScript());
		return bean;
	}

}
