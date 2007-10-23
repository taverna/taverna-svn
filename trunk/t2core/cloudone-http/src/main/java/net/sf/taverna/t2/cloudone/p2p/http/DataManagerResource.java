package net.sf.taverna.t2.cloudone.p2p.http;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.util.BeanSerialiser;

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

public class DataManagerResource extends Resource {

	private static Logger logger = Logger.getLogger(DataManagerResource.class);
	
	private String entityIdString;
	private DataManager dataManager;
	private EntityIdentifier entityId;
	private Entity<EntityIdentifier, ?> entity;

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
		Element xml = BeanSerialiser.beanableToXML(entity);
		String xmlString = new XMLOutputter(Format.getRawFormat())
				.outputString(xml);
		// TODO: Should use streaming
		// TODO: Should use a more portable format
		logger.debug("Sending " + entity);
		return new StringRepresentation(xmlString, MediaType.TEXT_XML);
	}
}
