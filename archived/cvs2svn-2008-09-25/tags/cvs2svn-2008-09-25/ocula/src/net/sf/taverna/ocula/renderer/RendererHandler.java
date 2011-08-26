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

package net.sf.taverna.ocula.renderer;

import net.sf.taverna.ocula.Ocula;
import org.apache.log4j.Logger;
import java.util.*;
import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import javax.swing.JComponent;

/**
 * Manages renderer services
 * @author Tom Oinn
 */
public class RendererHandler {
    
    private List renderers;
    private Ocula ocula;
    private static Logger log = Logger.getLogger(RendererHandler.class);
    
    /**
     * Create a registry of renderer SPI instances and attach to
     * the specified Ocula. We attach the registry to Ocula so
     * that renderers can access Ocula's surrounding context if
     * needed
     */
    public RendererHandler(Ocula ocula) {
	this.ocula = ocula;
	renderers = new ArrayList();
	log.info("Loading all renderers");
	SPInterface spiIF = new SPInterface(RendererSPI.class);
	ClassLoaders loaders = new ClassLoaders();
	loaders.put(Thread.currentThread().getContextClassLoader());
	Enumeration spe = Service.providers(spiIF, loaders);
	while (spe.hasMoreElements()) {
	    RendererSPI spi = (RendererSPI)spe.nextElement();
	    renderers.add(spi);
	    log.info("\t"+spi.getClass().toString());
	}
	log.info("Done");
    }
    
    /**
     * Get a JComponent for the specified Object, the first
     * renderer in the list to match will be returned.
     */
    public JComponent getRenderer(Object o) {
	for (Iterator i = renderers.iterator(); i.hasNext();) {
	    RendererSPI rspi = (RendererSPI)i.next();
	    if (rspi.canHandle(o, this.ocula)) {
		log.debug("Getting renderer " + rspi.getClass());
		return rspi.getRenderer(o, this.ocula);
	    }
	}
	return null;
    }
       

}
