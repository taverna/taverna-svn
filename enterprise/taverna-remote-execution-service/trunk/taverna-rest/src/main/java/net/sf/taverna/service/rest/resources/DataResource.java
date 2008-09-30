package net.sf.taverna.service.rest.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.rest.resources.representation.AbstractText;
import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.DataDocument;
import net.sf.taverna.t2.cloudone.rest.xml.DataMap;
import net.sf.taverna.t2.cloudone.rest.xml.Entity;
import net.sf.taverna.t2.cloudone.rest.xml.EntityList;
import net.sf.taverna.t2.cloudone.rest.xml.MapEntry;
import net.sf.taverna.t2.cloudone.rest.xml.StringLiteral;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.embl.ebi.escience.baclava.DataThing;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class DataResource extends AbstractDataResource {

	private static Logger logger = Logger.getLogger(DataResource.class);

	public DataResource(Context context, Request request, Response response) {
		super(context, request, response);
		setResource(dataDoc);
		addRepresentation(new Baclava());
		addRepresentation(new XML());
	}

	class Baclava extends AbstractText {
		@Override
		public String getText() {
			return dataDoc.getBaclava();
		}

		@Override
		public MediaType getMediaType() {
			return baclavaType;
		}
	}

	class XML extends AbstractOwnedXML<Data> {

		@Override
		public DataDocument createDocument() {
			DataDocument doc = DataDocument.Factory.newInstance();
			element = doc.addNewData();
			return doc;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addElements(Data dataElem) {
			super.addElements(dataElem);

			XmlObject xml;
			// FIXME: Don't include baclava document anymore?
			try {
				xml = XmlObject.Factory.parse(dataDoc.getBaclava());
				dataElem.addNewBaclava().set(xml);
			} catch (XmlException e) {
				logger.warn("Could not parse baclava for " + dataDoc, e);
			}

			DataMap portsElem = dataElem.addNewPorts();

			Map<String, DataThing> map = null;
			try {
				map = dataDoc.getDataMap();
			} catch (ParseException e) {
				logger.error("Invalid data document in " + dataDoc);
				return;
			}
			for (Entry<String, DataThing> port : map.entrySet()) {
				MapEntry entry = portsElem.addNewEntry();
				String portName = port.getKey();
				entry.setKey(portName);
				DataThing dataThing = port.getValue();
				Object dataObject = dataThing.getDataObject();
				String lsid = dataThing.getLSID(dataObject);
				
				// FIXME: Use index instead, since not all data
				// have an LSID!
				if (lsid.equals("")) {
					lsid = ";unknown";
				}
				
				Entity entity; 
				if (dataObject instanceof Collection) {
					String collectionURI = uriFactory.getURIEntityList(dataDoc,
							portName, lsid);
					EntityList entityList = EntityList.Factory.newInstance();
					entity = entityList;
					entityList.setHref(collectionURI);
					entityList.setDepth(getDepth(dataThing));
					entityList.setItems(((Collection) dataObject).size());
				} else {
					// Assume string-able
					String entityURI = uriFactory.getURIStringLiteral(dataDoc,
							portName, lsid);
					StringLiteral literal = StringLiteral.Factory.newInstance();
					entity = literal;
					literal.setHref(entityURI);
				}
				entry.setEntity(entity);
			}

		}

	}

	private static long getDepth(DataThing dataThing) {
		Pattern syntacticPattern = Pattern.compile("(l\\()'[^']*'\\)*");
		Matcher matcher = syntacticPattern
				.matcher(dataThing.getSyntacticType());
		return matcher.groupCount();
	}

	@Override
	public long maxSize() {
		return DataDoc.BACLAVA_MAX;
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void put(Representation entity) {
		if (!restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
					"Content type must be " + restType);
			return;
		}
		if (overMaxSize(entity)) {
			logger.warn("Uploaded datadoc was too large");
			return;
		}
		DataDocument dataDocument;
		String text = "";
		try {
			text = entity.getText();
			dataDocument = DataDocument.Factory.parse(text);
		} catch (XmlException e) {
			logger.warn("Could not parse XML\n" + text + "\n", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Could not parse XML");
			return;
		} catch (IOException e) {
			logger.warn("Could not read XML", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Could not read XML");
			return;
		}
		Data data = dataDocument.getData();
		if (data.getOwner() != null) {
			String ownerURI = data.getOwner().getHref();
			User owner = null;
			if (ownerURI != null) {
				owner = uriToDAO.getResource(ownerURI, User.class);
			}
			dataDoc.setOwner(owner);
		}
		if (data.getBaclava() != null) {
			dataDoc.setBaclava(data.getBaclava().xmlText());
		}
		daoFactory.commit();
		logger.info("Updated " + dataDoc);
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}

}
