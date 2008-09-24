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

package net.sf.taverna.interaction.server.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.interaction.server.PatternRegistry;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Uses the PatternRegistry to build a 'live' view of the currently available
 * InteractionPattern instances on this server
 * 
 * @author Tom Oinn
 */
public class PatternServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(PatternServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		SubmitServlet.getServer().setBaseURL(request.getServletPath());
		try {
			Document patternDoc = PatternRegistry.getPatternsAsXML();
			XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			String contents = xo.outputString(patternDoc);
			PrintWriter out = response.getWriter();
			response.setContentType("text/xml");
			out.write(contents);
			out.flush();
		} catch (IOException ioe) {
			log.error(ioe);
		}
	}

}
