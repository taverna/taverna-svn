/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.cloudone.p2p.http;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * RESTful version of a {@link DataManager}. Allows RESTful queries to retrieve
 * {@link Entity}s
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class DataManagerResource extends Resource {

	private static Logger logger = Logger.getLogger(DataManagerResource.class);

	private String entityIdString;
	private DataManager dataManager;
	private EntityIdentifier entityId;
	private Entity<EntityIdentifier, ?> entity;

	private BeanSerialiser beanSerialiser = BeanSerialiser.getInstance();

	public DataManagerResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
		entityIdString = request.getResourceRef().getRemainingPart();

		dataManager = (DataManager) context.getAttributes().get("dataManager");
		try {
			entityId = EntityIdentifiers.parse(entityIdString);
		} catch (MalformedIdentifierException ex) {
			logger.warn("Invalid identifier " + entityIdString, ex);
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					"Invalid identifier: " + entityIdString);
			return;
		}
		try {
			entity = dataManager.getEntity(entityId);
		} catch (RetrievalException e) {
			logger.warn("Could not retrieve " + entityId, e);
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			return;
		} catch (NotFoundException e) {
			logger.info("Could not find " + entityId, e);
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Element xml;
		xml = beanSerialiser.beanableToXMLElement(entity);
		String xmlString = new XMLOutputter(Format.getRawFormat())
				.outputString(xml);
		// TODO: Should use streaming
		// TODO: Should use a more portable format
		logger.debug("Sending " + entity);
		return new StringRepresentation(xmlString, MediaType.TEXT_XML);
	}
}
