package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.Port;

import uk.ac.mrc.hgmp.taverna.retsina.ScuflPort;
import java.lang.Object;



public class ScuflInputPort extends ScuflPort {

    Object userObject;

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
