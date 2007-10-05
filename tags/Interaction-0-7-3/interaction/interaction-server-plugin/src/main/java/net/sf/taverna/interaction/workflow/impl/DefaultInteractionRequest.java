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

package net.sf.taverna.interaction.workflow.impl;

import java.util.Date;
import java.util.Map;

import net.sf.taverna.interaction.workflow.InteractionPattern;
import net.sf.taverna.interaction.workflow.InteractionRequest;

import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Implementation of the InteractionRequest interface that can be used to submit
 * requests to the HTTPInteractionServiceProxy
 * 
 * @author Tom Oinn
 */
public class DefaultInteractionRequest implements InteractionRequest {

	private InteractionPattern pattern;

	private String email;

	private Map inputData;

	private Date expiryTime;

	/**
	 * Create an interaction request
	 * 
	 * @param pattern
	 *            The interaction pattern to use
	 * @param email
	 *            Valid email address for the person to be contacted
	 * @param inputData
	 *            Map of name -> DataThing object to use as input
	 * @param expiryTime
	 *            Time at which the request should be regarded as having expired
	 * @return an InteractionRequest object that can be submitted to an
	 *         InteractionService
	 */
	public static InteractionRequest createRequest(InteractionPattern pattern,
			String email, Map inputData, Date expiryTime) {
		return new DefaultInteractionRequest(pattern, email, inputData,
				expiryTime);
	}

	private DefaultInteractionRequest(InteractionPattern pattern, String email,
			Map inputData, Date expiryTime) {
		this.pattern = pattern;
		this.email = email;
		this.inputData = inputData;
		this.expiryTime = expiryTime;
	}

	public Date getExpiryTime() {
		return this.expiryTime;
	}

	public String getEmail() {
		return this.email;
	}

	public InteractionPattern getPattern() {
		return this.pattern;
	}

	/**
	 * Use the DataThingXMLFactory and helpers to generate the data map.
	 */
	public byte[] getData() {
		Document doc = DataThingXMLFactory.getDataDocument(inputData);
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		return xo.outputString(doc).getBytes();
	}

}
