package net.sf.taverna.t2.cloudone.util.spi;

/**
 * A simple, listed implementation of {@link DummySPI}.
 * 
 * @author Stian Soiland
 * 
 */
public class FirstDummySPI implements DummySPI {
	public String getName() {
		return "FirstDummySPI";
	}

}
