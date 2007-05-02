package net.sf.taverna.t2.cloudone;

/**
 * Defines locational context information mandated by a single type of reference
 * scheme.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface LocationalContext {

	public String getContextType();
	
	public String getValue(String... keyPath);
	
}
