/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Implementation of ProcessorFactory that creates the MobyParserDatatype node
 * 
 * @author Eddie Kawas
 */
public class MobyParseDatatypeProcessorFactory extends ProcessorFactory {

	private MobyParseDatatypeProcessor processor = null;

	private String endpoint = "";
	private String articleName = "";
	private String datatypeName = "";

	/**
	 * Create a new factory
	 */
	public MobyParseDatatypeProcessorFactory() {
		setName("Moby Datatype Parser");

	}

	/**
	 * Create a new factory
	 */
	public MobyParseDatatypeProcessorFactory(MobyParseDatatypeProcessor proc) {
		setName("Moby Datatype Parser");
		this.processor = proc;
	}

	/**
	 * 
	 * @return the processor
	 */
	public MobyParseDatatypeProcessor getProcessor() {
		return this.processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.ProcessorFactory#getProcessorDescription()
	 */
	public String getProcessorDescription() {
		return "A processor that allows the decomposition of BioMoby datatypes";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.ProcessorFactory#getProcessorClass()
	 */
	public Class getProcessorClass() {
		return org.biomoby.client.taverna.plugin.MobyParseDatatypeProcessor.class;
	}

	public String getArticleName() {
		return articleName;
	}

	public String getDatatypeName() {
		return datatypeName;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}

	public void setDatatypeName(String datatypeName) {
		this.datatypeName = datatypeName;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
