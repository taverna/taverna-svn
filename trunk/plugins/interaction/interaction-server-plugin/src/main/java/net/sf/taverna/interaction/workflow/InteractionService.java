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

package net.sf.taverna.interaction.workflow;

/**
 * Proxy to an Interaction Service server
 * 
 * @author Tom Oinn
 */
public interface InteractionService {

	/**
	 * Submit an interaction request to the server
	 * 
	 * @return an InteractionReceipt object which can be used to obtain results
	 *         and status on the interaction
	 */
	public InteractionReceipt submitRequest(InteractionRequest request)
			throws SubmissionException;

	/**
	 * Get all supported interaction patterns from this server
	 */
	public InteractionPattern[] getInteractionPatterns();

	/**
	 * Get the hostname of the remote interaction server
	 */
	public String getHostName();

}
