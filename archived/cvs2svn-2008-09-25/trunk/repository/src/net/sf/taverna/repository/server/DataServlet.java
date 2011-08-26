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

package net.sf.taverna.repository.server;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import org.embl.ebi.escience.scuflui.StreamCopier;

import java.io.*;

/**
 * Serves back thumbnails, images, summaries and definitions
 * @author Tom Oinn
 */
public class DataServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws ServletException {
	try {
	    String id = request.getParameter("id");
	    String type = request.getParameter("type");
	    Repository r = SubmitServlet.getRepository();
	    File f;
	    if (type.equals("thumb")) {
		response.setContentType("image/png");
		f = new File(r.getLocation(),id+".thumbnail.png");
	    }
	    else if (type.equals("image")) {
		response.setContentType("image/png");
		f = new File(r.getLocation(),id+".png");
	    }
	    else if (type.equals("definition")) {
		response.setContentType("text/xml");
		f = new File(r.getLocation(),id+".xml");
	    }
	    else {
		// Assume summary for when there's no defined type
		response.setContentType("text/html");
		f = new File(r.getLocation(),id+".summary.html");
	    }
	    Thread t = new StreamCopier(new FileInputStream(f), response.getOutputStream());
	    t.start();
	    t.join();
	    response.flushBuffer();
	}
	catch (Exception ioe) {
	    throw new ServletException("IO Error!",ioe);
	}
    }

}
