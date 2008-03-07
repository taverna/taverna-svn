package net.sf.taverna.t2.spi;

import static junit.framework.Assert.assertNotNull;

/**
 * An listed implementation of {@link DummySPI} which constructor that should be
 * called.
 * 
 * @author Stian Soiland
 * 
 */
public class SecondDummySPI implements DummySPI {

	private String name = null;

	public SecondDummySPI() {
		name = getClass().getSimpleName();
	}

	public String getName() {
		assertNotNull("Constructor was not called", name);
		return name;
	}
}
