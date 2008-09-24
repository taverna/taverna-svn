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

package net.sf.taverna.ocula.frame;

import org.jdom.Element;
import net.sf.taverna.ocula.Ocula;
import java.util.*;
import java.awt.*;
import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.log4j.Logger;

/**
 * Manages the instantiation of OculaFrame objects from the XML definition
 * when bound to an instance of Ocula, acts as a registry for the FrameSPI
 * @author Tom Oinn
 */
public class FrameHandler {

    private Map frameBuilders;
    private Ocula ocula;
    private static Logger log = Logger.getLogger(FrameHandler.class);
    
    /**
     * Create, load registry and attach to the specified Ocula
     */
    public FrameHandler(Ocula ocula) {
	this.ocula = ocula;
	frameBuilders = new HashMap();
	log.info("Loading all frame handlers");
	SPInterface spiIF = new SPInterface(FrameSPI.class);
	ClassLoaders loaders = new ClassLoaders();
	loaders.put(Thread.currentThread().getContextClassLoader());
	Enumeration spe = Service.providers(spiIF, loaders);
	while (spe.hasMoreElements()) {
	    FrameSPI spi = (FrameSPI)spe.nextElement();
	    frameBuilders.put(spi.getElementName(), spi);
	    log.info("\t"+spi.getElementName()+" -> "+spi.getClass().toString());
	}
	log.info("Done");
    }
    
    /**
     * Create a new Component implementing the OculaFrame interface, we enforce
     * the test for 'instanceof Component' in this method so the class may be 
     * safely cast to Component, it will never fail if this method succeeds.
     * @return the OculaFrame or null if the element wasn't recognized
     */
    public OculaFrame getFrame(Element e) {
	FrameSPI fspi = (FrameSPI)frameBuilders.get(e.getName());
	if (fspi == null) {
	    log.warn("No frame handler found for "+e.getName());
	    return null;
	}
	log.info("Building frame for "+e.getName());
	OculaFrame of = fspi.makeFrame(ocula, e);
	if (of instanceof Component) {
	    return of;
	}
	else {
	    log.error("Ocula frame implementation "+of.getClass().toString()+
		      " is not a subclass of Component, this is wrong.");
	    return null;
	}
    }

}
