/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.action;

import org.jdom.Element;
import net.sf.taverna.ocula.Ocula;
import org.apache.log4j.Logger;
import java.util.*;
import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;

/**
 * Binds to an Ocula instance and is responsible for invoking action
 * blocks
 * @author Tom Oinn
 */
public class ActionRunner {

    private Ocula ocula;
    private Map actions;
    private static Logger log = Logger.getLogger(ActionRunner.class);
    
    /**
     * Create and attach to a specified Ocula
     */
    public ActionRunner(Ocula ocula) {
	this.ocula = ocula;
	actions = new HashMap();
	log.info("Loading all action handlers");
	SPInterface spiIF = new SPInterface(ActionSPI.class);
	ClassLoaders loaders = new ClassLoaders();
	loaders.put(Thread.currentThread().getContextClassLoader());
	Enumeration spe = Service.providers(spiIF, loaders);
	while (spe.hasMoreElements()) {
	    ActionSPI spi = (ActionSPI)spe.nextElement();
	    actions.put(spi.getElementName(), spi);
	    log.info("\t"+spi.getElementName()+" -> "+spi.getClass().toString());
	}
	log.info("Done");
    }
    
    /**
     * Call the appropriate actions for all child
     * elements of the specified Element object, using
     * the SPI mechanism to discover appropriate action
     * classes for each element
     * @return List of ActionException objects thrown by subtasks, if any
     */
    public List runAction(Element actionElement) {
	List exceptions = new ArrayList();
	List children = actionElement.getChildren();
	for (Iterator i = children.iterator(); i.hasNext();) {
	    Element action = (Element)i.next();
	    ActionSPI aspi = (ActionSPI)actions.get(action.getName());
	    if (aspi != null) {
		log.info("Calling action '"+action.getName()+"'");
		try {
		    aspi.act(this.ocula, action);
		}
		catch (ActionException ae) {
		    exceptions.add(ae);
		    log.warn("Action exception thrown", ae);
		    break;
		}
	    }
	    else {
		log.warn("No action found for tag '"+action.getName());
	    }
	}
	return exceptions;
    }


}
