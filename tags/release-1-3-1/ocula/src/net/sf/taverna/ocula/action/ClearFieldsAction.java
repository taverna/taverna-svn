/*
 * Copyright 2005 University of Manchester
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

import net.sf.taverna.ocula.Ocula;

import org.apache.log4j.Logger;

import bsh.EvalError;

/**
 * This action sets all the &lt;text&gt; elements inside an &lt;input&gt;
 * element to an empty string.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class ClearFieldsAction extends AbstractInputAction {

    private static Logger log = Logger.getLogger(ClearFieldsAction.class);
    
    /**
     * This class will process &lt;clear&gt; elements.
     */
    public String getElementName() {
	return "clearfields";
    }
    
    /**
     * Removes the contents of the document.
     */
    protected void runScript(Ocula ocula, String name) throws EvalError {
	String script = name + ".remove(offset, length)";
	log.debug("Running script: " + script);
	ocula.runScript(script, new String[0]);
    }
}
