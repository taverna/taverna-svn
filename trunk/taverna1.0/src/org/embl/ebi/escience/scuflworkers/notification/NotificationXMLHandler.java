/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */

package org.embl.ebi.escience.scuflworkers.notification;

import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.XScufl;

import org.jdom.Element;

/**
 * @author Justin Ferris
 * @author Ananth Krishna
 */
public class NotificationXMLHandler implements XMLHandler {
    public Element elementForProcessor(Processor processor) {
        NotificationProcessor cp = (NotificationProcessor) processor;
        Element element = new Element("notification", XScufl.XScuflNS);
        return element;
    } 

    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	        throws ProcessorCreationException, 
	               DuplicateProcessorNameException, 
	               XScuflFormatException {
        Element el = processorNode.getChild("notification");
        NotificationProcessor np = new NotificationProcessor(model, name);
        return np;
    }
}