package org.embl.ebi.escience.taverna.retsina;

import com.jgraph.graph.Port;

import org.embl.ebi.escience.taverna.retsina.ScuflPort;
import java.lang.Object;



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
