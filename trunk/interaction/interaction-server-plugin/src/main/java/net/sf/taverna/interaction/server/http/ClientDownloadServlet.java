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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.interaction.server.InteractionServer;
import net.sf.taverna.interaction.server.InteractionState;
import net.sf.taverna.interaction.server.ServerInteractionPattern;

/**
 * Servlet to allow interaction clients access to the input values of an
 * interaction job, uses the ServerInteractionPattern in the InteractionState to
 * translate from the on disk XML form to one suited to that particular pattern.
 * 
 * @author Tom Oinn
 */
public class ClientDownloadServlet extends AbstractClientServlet {

	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException {
		ServerInteractionPattern pattern = state.getInteractionPattern();
		pattern.handleInputDownload(request, response, state, server);
	}

}
