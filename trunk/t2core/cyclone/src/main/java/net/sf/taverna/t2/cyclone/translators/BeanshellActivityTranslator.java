package net.sf.taverna.t2.cyclone.translators;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;


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
		
		//FIXME: what if we are casting to (or from) the wrong version of a BeanshellProcessor??
		bean.setScript(((BeanshellProcessor)processor).getScript());
		return bean;
	}

}
