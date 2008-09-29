package net.sf.taverna.t2.spi;

/**
 * Dummy SPI interface for {@link SPIRegistryTest}.
 * 
 * @author Stian Soiland
 *
 */
public interface DummySPI {
	/**
	 * Should return the name of the class, for instance "DummySPI1"
	 * 
	 * @return Name of the class
	 */
	public String getName();
}
