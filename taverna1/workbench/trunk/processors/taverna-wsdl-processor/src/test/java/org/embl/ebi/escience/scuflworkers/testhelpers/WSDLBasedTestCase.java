package org.embl.ebi.escience.scuflworkers.testhelpers;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

/**
 * Base class for Unit Tests based upon wsdls. Contains some common useful
 * utilities.
 * 
 * @author Stuart Owen
 * 
 */

public abstract class WSDLBasedTestCase {
	/**
	 * should only be used in integration tests
	 */
	@Deprecated
	protected final String TESTWSDL_BASE = "http://www.mygrid.org.uk/taverna-tests/testwsdls/";

	protected WSDLBasedProcessor createProcessor(String wsdl, String operation)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		WSDLBasedProcessor processor = new WSDLBasedProcessor(null, "test",
				wsdl, operation);
		return processor;
	}
}
