package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.Port;

public class ScuflInputPort extends ScuflPort {

    public ScuflInputPort() {
	super();
    }

    public ScuflInputPort(Object userObject) {
        super(userObject);
    }
    
    public ScuflInputPort(Object userObject, Port anchor) {
        super(userObject, anchor);
    }

}
