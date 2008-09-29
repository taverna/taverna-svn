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
 * Thrown by instances of the PageValidationSPI when an element
 * within a page definition violates one of the validation rules
 * @author Tom Oinn
 */
public class PageValidationException extends Exception {
    
    private Element problemElement;
    
    /**
     * Create a new PageValidationException
     * @param problemElement the Element that caused the validation failure
     * @param message textual description of the failure
     */
    public PageValidationException(Element problemElement, 
				   String message) {
	super(message);
	assert problemElement != null;
	this.problemElement = problemElement;
    }
 
    /**
     * Create a new PageValidationException
     * @param problemElement the Element that caused the validation failure
     * @param message textual description of the failure
     * @param cause underlying Exception
     */
    public PageValidationException(Element problemElement,
				   String message,
				   Exception cause) {
	super(message);
	assert cause != null;
	initCause(cause);
	assert problemElement != null;
	this.problemElement = problemElement;
    }
   
    /**
     * Return the source of the problem
     * @return the Element that caused the validation exception
     */
    public Element getSource() {
	return this.problemElement;
    }

}
