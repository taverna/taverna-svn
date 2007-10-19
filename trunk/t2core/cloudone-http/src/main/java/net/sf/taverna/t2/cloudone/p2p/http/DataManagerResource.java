package net.sf.taverna.t2.cloudone.p2p.http;

import java.beans.XMLEncoder;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.util.BeanSerialiser;

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

	private String entityIdString;
	private DataManager dataManager;
	private EntityIdentifier entityId;
	private Entity<EntityIdentifier, ?> entity;

	public DataManagerResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));
		entityIdString = request.getResourceRef().getRemainingPart();
		System.out.println("entityId: " + entityIdString);

		dataManager = (DataManager) context.getAttributes().get("dataManager");
		try {
			entityId = EntityIdentifiers.parse(entityIdString);
		} catch (MalformedIdentifierException ex) {
			ex.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					"Invalid identifier: " + entityIdString);
			return;
		}
		System.out.println("Parsed " + entityId);
		try {
			entity = dataManager.getEntity(entityId);
		} catch (RetrievalException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			return;
		} catch (NotFoundException e) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}

		System.out.println("The entity is " + entity);
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		System.out.println("Asked for " + variant);
		Element xml = BeanSerialiser.toXML(entity.getAsBean());
		String xmlString = new XMLOutputter(Format.getRawFormat())
				.outputString(xml);
		// TODO: Should use streaming
		// TODO: Should use a more portable format
		return new StringRepresentation(xmlString, MediaType.TEXT_XML);
	}
}
