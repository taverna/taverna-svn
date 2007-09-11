package net.sf.taverna.t2.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;

public class BeanshellServiceTranslator implements ServiceTranslator<BeanshellConfigurationType> {

	public Activity<BeanshellConfigurationType> doTranslation(Processor p) {
		// TODO Auto-generated method stub
		return new BeanshellService();
	}

}
