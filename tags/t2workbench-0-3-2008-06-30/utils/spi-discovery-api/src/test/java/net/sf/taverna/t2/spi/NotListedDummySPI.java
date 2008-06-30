package net.sf.taverna.t2.spi;

import static junit.framework.Assert.fail;

/**
 * Dummy SPI implementation that is <strong>not</strong> listed in the
 * net.sf.taverna.t2.cloudone.util.spi.DummySPI file, and should not be found by
 * {@link SPIRegistry#getInstances()}.
 * 
 * @author Stian Soiland
 * 
 */
public class NotListedDummySPI implements DummySPI {
	public String getName() {
		fail("NotListedDummySPI should not have been included");
		return "NotListedDummySPI";
	}
}
