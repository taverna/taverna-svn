package uk.org.taverna.scufl2.rdf.port;

import org.openrdf.elmo.annotations.rdf;

import uk.org.taverna.scufl2.rdf.common.Ontology;
import uk.org.taverna.scufl2.rdf.iterationstrategy.IterationStrategyNode;


@rdf(Ontology.CORE + "InputProcessorPort")
public interface InputProcessorPort extends IterationStrategyNode,
		ReceiverPort, ProcessorPort {
	public void setGranularDepth(Integer granularDepth);

	@rdf(Ontology.CORE + "granularDepth")
	public Integer getGranularDepth();
}
