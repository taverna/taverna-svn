package net.sf.taverna.t2.cloudone.util.spi;

import static junit.framework.Assert.fail;

/**
 * Although this class implements the right methods and is listed in the file,
 * it should be ignored without causing any ClassCastExceptions. (But should be
 * logged!)
 * 
 * @author Stian Soiland
 * 
 */
public class NotImplementingDummySPI {
	
	public String getName() {
		fail("NotImplementingDummySPI should not have been included");
		return "NotImplementingDummySPI";
	}

}
