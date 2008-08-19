package net.sf.taverna.t2.workbench.models.graph.svg.event;

import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;

import org.w3c.dom.events.EventListener;

/**
 * Abstract superclass for SVG envent listeners.
 * 
 * @author David Withers
 */
public abstract class SVGEventListener implements EventListener {

	protected GraphEventManager graphEventManager;
	
	protected GraphElement graphElement;
	
	public SVGEventListener(GraphEventManager graphEventManager,
			GraphElement graphElement) {
		this.graphEventManager = graphEventManager;
		this.graphElement = graphElement;
	}
	
}
