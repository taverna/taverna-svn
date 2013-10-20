// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getResourceCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class User extends Resource {
	private static final long serialVersionUID = -6143018077882192560L;

	private String name;
	private final List<Map<String, String>> workflows = new ArrayList<Map<String, String>>();
	private final List<Map<String, String>> files = new ArrayList<Map<String, String>>();
	private final List<Map<String, String>> packs = new ArrayList<Map<String, String>>();
	private final List<Map<String, String>> groups = new ArrayList<Map<String, String>>();

	private static final Cache<User> existing = new Cache<User>();

	public User() {
		super(Type.USER);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setTitle(name); // this will allow to use name/title interchangeably
	}

	public List<Map<String, String>> getWorkflows() {
		return workflows;
	}

	public List<Map<String, String>> getFiles() {
		return files;
	}

	public List<Map<String, String>> getPacks() {
		return packs;
	}

	public List<Map<String, String>> getGroups() {
		return groups;
	}

	/**
	 * A helper method to return a set of API elements that are needed to
	 * satisfy request of a particular type - e.g. creating a listing of
	 * resources or populating full preview, etc.
	 * 
	 * @param requestType
	 *            A constant value from Resource class.
	 * @return Comma-separated string containing values of required API
	 *         elements.
	 */
	@SuppressWarnings("incomplete-switch")
	public static String getRequiredAPIElements(RequestType requestType) {
		String strElements = "";

		/*
		 * cases higher up in the list are supersets of those that come below -
		 * hence no "break" statements are required, because 'falling through'
		 * the switch statement is the desired behaviour in this case;
		 * 
		 * cases after first 'break' statement are separate ones and hence are
		 * treated individually
		 */
		switch (requestType) {
		case PREVIEW:// email,website,city,country,friends,tags-applied,favourited,
			strElements += "created-at,updated-at,groups,workflows,files,packs,";
		case FULL_LISTING:
			// essentially the same as short listing
		case SHORT_LISTING:// ,avatar
			strElements += "id,name,description";
			break;
		case FAVOURITES:
			strElements += "favourited";
			break;
		case TAGS:
			strElements += "tags-applied";
			break;
		}

		return strElements;
	}

	public static User buildFromXML(Document doc, Logger logger) {
		// if no XML document was supplied, return NULL
		if (doc == null)
			return null;

		// call main method which parses XML document starting from root element
		return buildFromXML(doc.getDocumentElement(), logger);
	}

	// class method to build a user instance from XML
	public static User buildFromXML(Element root, Logger logger) {
		// can't make any processing if root element is NULL
		if (root == null)
			return null;

		// create instance and parse the XML otherwise
		User user = new User();

		try {
			// store all simple values
			user.setURI(root.getAttribute("uri"));
			user.setResource(root.getAttribute("resource"));
			user.setID(root, logger);
			user.setName(getChildText(root, "name"));
			/*
			 * to allow generic handling of all resources - for users 'title'
			 * will replicate the 'name'
			 */
			user.setTitle(user.getName());
			user.setDescription(getChildText(root, "description"));

			// Created at
			user.setCreatedAt(getChildText(root, "created-at"));

			// Updated at
			user.setUpdatedAt(getChildText(root, "updated-at"));

			// store workflows
			Element workflows = getChild(root, "workflows");
			if (workflows != null)
				getResourceCollection(workflows.getChildNodes(), user.workflows);

			// store files
			Element files = getChild(root, "files");
			if (files != null)
				getResourceCollection(files.getChildNodes(), user.files);

			// store packs
			Element packs = getChild(root, "packs");
			if (packs != null)
				getResourceCollection(packs.getChildNodes(), user.packs);

			// store groups
			Element groups = getChild(root, "groups");
			if (groups != null)
				getResourceCollection(groups.getChildNodes(), user.groups);
			existing.put(user);
		} catch (Exception e) {
			logger.error("Failed midway through creating user object from XML",
					e);
		}

		return user;
	}

	static User getExisting(Element elem) {
		return existing.get(elem);
	}
}
