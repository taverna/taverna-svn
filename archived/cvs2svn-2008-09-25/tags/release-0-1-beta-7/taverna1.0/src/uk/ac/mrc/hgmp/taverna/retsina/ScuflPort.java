package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.Port;




public abstract class ScuflPort extends DefaultPort {

    private org.embl.ebi.escience.scufl.Port scuflPort;

    public ScuflPort() {
	super();
    }

    public ScuflPort(Object userObject) {
        super(userObject);
    }

    public ScuflPort(Object userObject, Port anchor) {
        super(userObject, anchor);
    }

    public ScuflPort(Object userObject, org.embl.ebi.escience.scufl.Port scuflPort){
        super(userObject);
        this.scuflPort = scuflPort;
    }

    public org.embl.ebi.escience.scufl.Port getScuflPort(){
        return scuflPort;
    }

}
