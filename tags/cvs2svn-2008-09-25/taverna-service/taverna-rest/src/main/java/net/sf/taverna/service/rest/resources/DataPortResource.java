package net.sf.taverna.service.rest.resources;

import net.sf.taverna.t2.cloudone.rest.xml.DataMap;
import net.sf.taverna.t2.cloudone.rest.xml.DataMapDocument;
import net.sf.taverna.t2.cloudone.rest.xml.Entity;
import net.sf.taverna.t2.cloudone.rest.xml.EntityList;
import net.sf.taverna.t2.cloudone.rest.xml.EntityListDocument;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class DataPortResource extends AbstractDataResource {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataPortResource.class);
	
	public DataPortResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

		class XML extends AbstractREST<Entity> {
			
			@Override
			public EntityListDocument createDocument() {
				EntityListDocument doc = EntityListDocument.Factory.newInstance();
				element = doc.addNewEntityList();
				return doc;
			}
			
			@Override
			public void addElements(Entity dataElem) {
				//
			}

		
	}

}
