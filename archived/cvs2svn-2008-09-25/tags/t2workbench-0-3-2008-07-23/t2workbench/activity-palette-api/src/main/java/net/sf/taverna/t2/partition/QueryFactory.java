package net.sf.taverna.t2.partition;

import java.util.List;

/**
 * An SPI interface for QueryFactories.
 * 
 * @author Stuart Owen
 * @see QueryFactoryRegistry
 * 
 */
public interface QueryFactory {
	
	/**
	 * @return a list of Query objects
	 * @see Query
	 */
	List<Query<?>> getQueries();
	
}
