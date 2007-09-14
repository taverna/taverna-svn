package net.sf.taverna.t2.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;

public interface ActivityTranslator<ConfigurationType> {
	
	public Activity<ConfigurationType> doTranslation(Processor p);
	
}
