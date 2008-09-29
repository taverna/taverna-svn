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

/**
 * Defines an action over the page state, especially the page context.
 * @author Tom Oinn
 */
public interface ActionSPI {

    /**
     * @return the element name that this action is mapped to in the 
     * definition language.
     */
    public String getElementName();
    
    /**
     * Run this action over the specified page using any additional
     * information from the element that caused the action to be called
     * @throws ActionException if an exception is thrown during the
     * invocation of this particular action
     * @param page the Page object this is acting on
     * @param element the Element in the definition that caused the
     * action in the first place
     */
    public void act(Ocula ocula, Element element) throws ActionException;
    
}
