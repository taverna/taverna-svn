package org.embl.ebi.escience.scuflworkers.testhelpers;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

import junit.framework.TestCase;

/**
 * Base class for Unit Tests based upon wsdls. Contains some common useful utilities.
 * @author sowen
 *
 */

public abstract class WSDLBasedTestCase extends TestCase {
	protected final String TESTWSDL_BASE="http://www.cs.man.ac.uk/~sowen/tests/testwsdls/";
	
	protected WSDLBasedProcessor createProcessor(String wsdl, String operation) throws ProcessorCreationException, DuplicateProcessorNameException {
		WSDLBasedProcessor processor = new WSDLBasedProcessor(null, "test",
				wsdl,
				operation);
		return processor;
	}
}
