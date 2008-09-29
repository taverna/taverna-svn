package net.sourceforge.taverna.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This interface defines the basic method needed by all stream processors.
 * A stream processor is a delegate class that the StreamTransmitter uses
 * to process the result returned by a webserver request.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public interface StreamProcessor {
    
    /**
     * This method processes a result stream.
     * @param stream
     */
    public Map processStream(InputStream stream) throws IOException;

}
