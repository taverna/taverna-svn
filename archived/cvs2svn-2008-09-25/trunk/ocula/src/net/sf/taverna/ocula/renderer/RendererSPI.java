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
import javax.swing.JComponent;

/**
 * Defines logic to render an Object to a JComponent
 * @author Tom Oinn
 */
public interface RendererSPI {

    /**
     * @return whether this class can handle the specified object
     */
    public boolean canHandle(Object o, Ocula ocula);
    
    /**
     * Create a heavyweight JComponent (to allow for animation, selection
     * behaviour etc) representing the specified Object
     * @param object the Object to render
     * @return a JComponent rendering the specified object
     */
    public JComponent getRenderer(Object object, Ocula ocula);
    
}
