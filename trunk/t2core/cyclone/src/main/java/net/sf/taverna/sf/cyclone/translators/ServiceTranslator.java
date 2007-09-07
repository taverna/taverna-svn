package net.sf.taverna.sf.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.service.Service;

import org.embl.ebi.escience.scufl.Processor;

public interface ServiceTranslator<ConfigurationType> {
	
	public Service<ConfigurationType> doTranslation(Processor p);
	
}
