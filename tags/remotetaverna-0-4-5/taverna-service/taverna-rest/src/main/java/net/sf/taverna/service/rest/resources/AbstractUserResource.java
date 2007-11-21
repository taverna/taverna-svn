package net.sf.taverna.service.rest.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AbstractUserResource extends AbstractDatedResource<User> {

	User user;

	public AbstractUserResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		UserDAO dao = daoFactory.getUserDAO();
		String userName = (String) request.getAttributes().get("user");
		user = dao.readByUsername(userName);
		checkEntity(user);
	}

	class OwnedVelocity extends VelocityRepresentation {
		private Collection<?> collection;
		
		private String ownedType;

		public OwnedVelocity(Collection<?> collection, String ownedType) {
			this.collection = collection;
			this.ownedType = ownedType;
		}

		@Override
		protected String pageTitle() {
			return ownedType+" owned by "+user.getUsername();
		}

		@Override
		protected String templateName() {
			return "ownedCollection.vm";
		}

		@Override
		protected Map<String, Object> getDataModel() {
			Map<String,Object> model = new HashMap<String, Object>();			
			model.put("user", user);
			model.put("uriFactory", uriFactory);
			model.put("userURI", uriFactory.getURI(user));
			model.put("ownedType", ownedType);
			collection.size(); // pre-fetch
			model.put("collection", collection);
			return model;
		}
	}
	
}
