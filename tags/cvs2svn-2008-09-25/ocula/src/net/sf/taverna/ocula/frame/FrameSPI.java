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

/**
 * Self-contained panels that are responsible for dealing
 * with things like the layout and certain aspects of the appearance of their
 * components.
 * @author Tom Oinn
 */
public interface FrameSPI {

    /**
     * @return the element name that this frame is mapped to in the 
     * definition language.
     */
    public String getElementName();
    
    /**
     * Consume the supplied element in the context of the Ocula instance
     * and generate an instance of OculaFrame containing the results
     * of the query defined in the element.<p/>
     * No significant heavy lifting should be done in this method call,
     * for those frame objects which represent queries over the context
     * such queries must be done in their own thread, make the frames
     * smart enough to handle this.
     */
    public OculaFrame makeFrame(Ocula ocula, Element element);
    
}
