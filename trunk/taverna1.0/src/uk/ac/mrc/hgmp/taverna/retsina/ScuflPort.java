package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.Port;

import java.lang.Object;



public abstract class ScuflPort extends DefaultPort {

    public ScuflPort() {
	super();
    }

    public ScuflPort(Object userObject) {
        super(userObject);
    }

    public ScuflPort(Object userObject, Port anchor) {
        super(userObject, anchor);
    }

}
