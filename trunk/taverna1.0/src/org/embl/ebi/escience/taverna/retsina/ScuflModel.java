package org.embl.ebi.escience.taverna.retsina;

import com.jgraph.graph.DefaultGraphModel;
import com.jgraph.graph.Edge;

import java.lang.Object;



/**
 * Defines a Model which can contain the sanity checks on port
 * connections. We can put the type checking logic in here when
 * we have the information coming out of EMBOSS neatly.
 * @author Tom Oinn
 */
public class ScuflModel extends DefaultGraphModel {

    /**
     * Do not allow self references
     */
    public boolean acceptsSource(Object edge, Object port) {
	return (((Edge) edge).getTarget() != port);
    }
    
    /** 
     * Do not allow self references
     */
    public boolean acceptsTarget(Object edge, Object port) {
	return (((Edge) edge).getSource() != port);
    }

}
