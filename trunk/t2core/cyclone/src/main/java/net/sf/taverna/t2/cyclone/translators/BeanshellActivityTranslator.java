package net.sf.taverna.t2.cyclone.translators;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;


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
		
		bean.setScript(((BeanshellProcessor)processor).getScript());
		return bean;
	}

}
