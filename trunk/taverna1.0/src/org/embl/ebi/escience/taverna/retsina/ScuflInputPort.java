package org.embl.ebi.escience.taverna.retsina;

import com.jgraph.graph.Port;

import org.embl.ebi.escience.taverna.retsina.ScuflPort;
import java.lang.Object;



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
