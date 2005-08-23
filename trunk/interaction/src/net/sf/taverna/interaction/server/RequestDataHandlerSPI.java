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

package net.sf.taverna.interaction.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Implementations of this class are capable of reponding to the
 * HttpServletRequest and a File reference to previously uploaded
 * interaction request data and construcing an appropriate 
 * HttpServletResponse. This is predicated on a match on the 
 * pattern name.
 * @author Tom Oinn
 */
public interface RequestDataHandlerSPI {

    /**
     * Return true if this is the handler for the specified
     * pattern name.
     */
    public boolean isHandlerFor(String patternName);
    
    /**
     * Use the supplied objects to return whatever data the
     * client side interaction code requires from the specified
     * File. The servlet request is made available here in case
     * the client code needs to make multiple connections for
     * each item of data and specified additional parametes
     * within the request object
     */
    public void handle(HttpServletRequest request,
		       File uploadedData,
		       HttpServletResponse response);
    
}
