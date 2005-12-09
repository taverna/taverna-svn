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
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

/**
 * Action to move to another page
 * @author Tom Oinn
 */
public class JumpToPageAction implements ActionSPI {

    private static Logger log = Logger.getLogger(JumpToPageAction.class);

    public String getElementName() {
	return "load";
    }
    public void act(Ocula ocula, Element actionElement) throws ActionException {
	String newLocation = actionElement.getAttributeValue("url");
	if (newLocation != null) {
	    try {
		ocula.load(new URL(ocula.getCurrentURL(), newLocation));
	    }
	    catch (MalformedURLException mue) {
		log.error("Malformed URL Exception", mue);
		ActionException ae = new ActionException("Badly formed URL");
		ae.initCause(mue);
		throw ae;
	    }
	    catch (Exception ex) {
		log.error("Cannot load page", ex);
		ActionException ae = new ActionException("Unable to load page");
		ae.initCause(ex);
		throw ae;
	    }
	}
    }

}
