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

package net.sf.taverna.ocula.validation;

import org.jdom.Element;

/**
 * Defines a validation module - all modules are run each time a page
 * definition is loaded by the layout engine.
 * @author Tom Oinn
 */
public interface PageValidationSPI {

    /**
     * Apply this validation method to the specified Element
     * in the document
     * @throws PageValidationException if the validation fails
     * @param e the Element to validate
     */
    public void validate(Element e) throws PageValidationException;
    
    /**
     * Determines whether this validation method will be applied
     * to the specified element
     * @param e the Element object to which the validate() method
     * will be applied
     * @return whether this method is appropriate
     */
    public boolean accept(Element e);
    
}
