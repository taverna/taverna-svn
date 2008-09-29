package net.sourceforge.taverna.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class provides a useful extension point for Stream Processors.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public abstract class AbstractStreamProcessor implements StreamProcessor {

	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * @see net.sourceforge.taverna.io.StreamProcessor#processStream(java.io.InputStream)
	 */
	public abstract Map processStream(InputStream stream) throws IOException;

}
