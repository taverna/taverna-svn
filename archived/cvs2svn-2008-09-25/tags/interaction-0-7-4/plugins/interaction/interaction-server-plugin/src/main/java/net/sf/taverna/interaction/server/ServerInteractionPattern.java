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

import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.interaction.workflow.InteractionPattern;

/**
 * Extends the base InteractionPattern interface and adds the various properties
 * such as handler classes for input and output, URL builders to generate the
 * links in email messages etc
 * 
 * @author Tom Oinn
 */
public interface ServerInteractionPattern extends InteractionPattern {

	/**
	 * Generate a new invitation message for this interaction pattern
	 * 
	 * @param baseURL
	 *            the URL to the first servlet called within this installation,
	 *            should hopefully mean that URLs can be created for callback
	 *            webpages, applets, jnlp files etc.
	 * @param state
	 *            the interaction state for this request
	 */
	public String getMessageBody(URL baseURL, InteractionState state);

	/**
	 * Handle request for interaction input data, this method is passed the
	 * HttpServletRequest / Response objects from any GET or POST request to the
	 * datafetch servlet along with the InteractionState and InteractionServer
	 * objects for context
	 */
	public void handleInputDownload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException;

	/**
	 * Handle result upload from the client side interaction code, should
	 * message the state object appropriately if this corresponds to either
	 * successful or failed completion
	 */
	public void handleResultUpload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException;

}
