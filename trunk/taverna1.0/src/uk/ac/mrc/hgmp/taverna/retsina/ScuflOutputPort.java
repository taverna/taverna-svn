package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.Port;

public class ScuflOutputPort extends ScuflPort {

    public ScuflOutputPort() {
	super();
    }

    public ScuflOutputPort(Object userObject) {
        super(userObject);
    }
    
    public ScuflOutputPort(Object userObject, Port anchor) {
        super(userObject, anchor);
    }

}
