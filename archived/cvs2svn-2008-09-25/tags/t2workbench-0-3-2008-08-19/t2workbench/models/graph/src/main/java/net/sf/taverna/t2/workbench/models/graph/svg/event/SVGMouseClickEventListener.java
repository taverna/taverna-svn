package net.sf.taverna.t2.workbench.models.graph.svg.event;

import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGUtil;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.svg.SVGLocatable;

/**
 * SVG event listener for handling mouse click events.
 * 
 * @author David Withers
 */
public class SVGMouseClickEventListener extends SVGEventListener {

	public SVGMouseClickEventListener(GraphEventManager graphEventManager, GraphElement graphElement) {
		super(graphEventManager, graphElement);
	}

	public void handleEvent(Event evt) {
		if (evt instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) evt;
			SVGOMPoint point = SVGUtil.screenToDocument((SVGLocatable)evt.getTarget(),
					new SVGOMPoint(mouseEvent.getClientX(), mouseEvent.getClientY()));
			graphEventManager.mouseClicked(graphElement, mouseEvent.getButton(),
					mouseEvent.getAltKey(), mouseEvent.getCtrlKey(), mouseEvent.getMetaKey(),
					(int) point.getX(), (int) point.getY(),
					mouseEvent.getScreenX(), mouseEvent.getScreenY());
			evt.stopPropagation();
		}
	}
	
}
