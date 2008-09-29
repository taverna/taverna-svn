package org.embl.ebi.escience.scuflworkers.java;

import org.jdom.Element;

/**
 * Inteface that indicates that a LocalWorker XML description can be extended
 * within an addional <extensions/> element, and that the LocalWorker can
 * generate and consume this XML.
 */

public interface XMLExtensible {
	public void consumeXML(Element element);

	public Element provideXML();
}
