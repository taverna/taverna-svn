package net.sf.taverna.t2.spi;

import static junit.framework.Assert.fail;

/**
 * This class implements DummySPI and is in the file, but does not have the
 * default constructor, and should be ignored (with a warning).
 * 
 * @author Stian Soiland
 * 
 */
public class MissingConstructor implements DummySPI {

	public MissingConstructor(String something) {
		fail("MissingConstructor should not have been constructed");
	}
	
	public String getName() {
		fail("MissingConstructor should not have been constructed");
		return "MissingConstructor";
	}

}
