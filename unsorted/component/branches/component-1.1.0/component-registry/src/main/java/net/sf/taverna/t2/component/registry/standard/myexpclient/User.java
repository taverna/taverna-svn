// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import static net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient.parseDate;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getResourceCollection;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveUserFavourites;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class User extends Resource {
	private static final long serialVersionUID = -6143018077882192560L;

	private String name;
	private String city;
	private String country;
	private String email;
	private String website;

	private ImageIcon avatar;
	private String avatar_uri;
	private String avatar_resource;

	private List<Map<String, String>> workflows;
	private List<Map<String, String>> files;
	private List<Map<String, String>> packs;
	private List<Map<String, String>> friends;
	private List<Map<String, String>> groups;
	private List<Map<String, String>> tags;
	private List<Resource> favourites;

	public User() {
		super();
		this.setItemType(USER);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setTitle(name); // this will allow to use name/title interchangeably
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getAvatarURI() {
		return avatar_uri;
	}

	public void setAvatarURI(String avatar_uri) {
		this.avatar_uri = avatar_uri;
	}

	public ImageIcon getAvatar() {
		return avatar;
	}

	// creates avatar from the XML of it
	public void setAvatar(Document doc) {
		Element root = doc.getDocumentElement();
		String avatarData = root.getElementsByTagName("data").item(0)
				.getTextContent();

		avatar = new ImageIcon(Base64.decode(avatarData));
	}

	public void setAvatar(ImageIcon avatar) {
		this.avatar = avatar;
	}

	public String getAvatarResource() {
		return avatar_resource;
	}

	public void setAvatarResource(String avatar_resource) {
		this.avatar_resource = avatar_resource;
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

	public List<Map<String, String>> getFriends() {
		return friends;
	}

	public List<Map<String, String>> getGroups() {
		return groups;
	}

	public List<Resource> getFavourites() {
		return favourites;
	}

	public List<Map<String, String>> getTags() {
		return this.tags;
	}

	/**
	 * A helper method to return a set of API elements that are needed to
	 * satisfy request of a particular type - e.g. creating a listing of
	 * resources or populating full preview, etc.
	 * 
	 * @param iRequestType
	 *            A constant value from Resource class.
	 * @return Comma-separated string containing values of required API
	 *         elements.
	 */
	public static String getRequiredAPIElements(int iRequestType) {
		String strElements = "";

		// cases higher up in the list are supersets of those that come below -
		// hence no "break" statements are required, because 'falling through'
		// the
		// switch statement is the desired behaviour in this case;
		//
		// cases after first 'break' statement are separate ones and hence are
		// treated
		// individually
		switch (iRequestType) {
		case REQUEST_FULL_PREVIEW:
			strElements += "created-at,updated-at,email,website,city,country,"
					+ "friends,groups,workflows,files,packs,favourited,tags-applied,";
		case REQUEST_FULL_LISTING:
			strElements += ""; // essentially the same as short listing
		case REQUEST_SHORT_LISTING:
			strElements += "id,name,description,avatar";
			break;
		case REQUEST_USER_FAVOURITES_ONLY:
			strElements += "favourited";
			break;
		case REQUEST_USER_APPLIED_TAGS_ONLY:
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
	public static User buildFromXML(Element docRootElement, Logger logger) {
		// can't make any processing if root element is NULL
		if (docRootElement == null)
			return (null);

		// create instance and parse the XML otherwise
		User user = new User();

		try {
			// store all simple values
			user.setURI(docRootElement.getAttribute("uri"));
			user.setResource(docRootElement.getAttribute("resource"));
			user.setID(getChildText(docRootElement, "id"));
			user.setName(getChildText(docRootElement, "name"));
			user.setTitle(user.getName()); // to allow generic handling of all
											// resources - for users 'title'
											// will replicate the 'name'
			user.setDescription(getChildText(docRootElement, "description"));
			user.setCity(getChildText(docRootElement, "city"));
			user.setCountry(getChildText(docRootElement, "country"));
			user.setEmail(getChildText(docRootElement, "email"));
			user.setWebsite(getChildText(docRootElement, "website"));

			// avatar URI in the API
			Element avatarURIElement = (Element) docRootElement
					.getElementsByTagName("avatar").item(0);
			if (avatarURIElement != null)
				user.setAvatarURI(avatarURIElement.getAttribute("uri"));

			// avatar resource on myExperiment
			Element avatarElement = getChild(docRootElement, "avatar");
			if (avatarElement != null)
				user.setAvatarResource(avatarElement.getAttribute("resource"));

			// Created at
			String createdAt = getChildText(docRootElement, "created-at");
			if (createdAt != null && !createdAt.equals(""))
				user.setCreatedAt(parseDate(createdAt));

			// Updated at
			String updatedAt = getChildText(docRootElement, "updated-at");
			if (updatedAt != null && !updatedAt.equals(""))
				user.setUpdatedAt(parseDate(updatedAt));

			// store workflows
			user.workflows = new ArrayList<Map<String, String>>();
			Element workflowsElement = getChild(docRootElement, "workflows");
			if (workflowsElement != null)
				getResourceCollection(workflowsElement.getChildNodes(),
						user.workflows);

			// store files
			user.files = new ArrayList<Map<String, String>>();
			Element filesElement = getChild(docRootElement, "files");
			if (filesElement != null)
				getResourceCollection(filesElement.getChildNodes(), user.files);

			// store packs
			user.packs = new ArrayList<Map<String, String>>();
			Element packsElement = getChild(docRootElement, "packs");
			if (packsElement != null)
				getResourceCollection(packsElement.getChildNodes(), user.packs);

			// store friends
			user.friends = new ArrayList<Map<String, String>>();
			Element friendsElement = getChild(docRootElement, "friends");
			if (filesElement != null)
				getResourceCollection(friendsElement.getChildNodes(),
						user.friends);

			// store groups
			user.groups = new ArrayList<Map<String, String>>();
			Element groupsElement = getChild(docRootElement, "groups");
			if (groupsElement != null)
				getResourceCollection(groupsElement.getChildNodes(),
						user.groups);

			// store tags
			user.tags = new ArrayList<Map<String, String>>();
			Element tagsElement = getChild(docRootElement, "tags-applied");
			if (tagsElement != null)
				getResourceCollection(tagsElement.getChildNodes(), user.tags);

			// store favourites
			user.favourites = new ArrayList<Resource>();
			user.favourites.addAll(retrieveUserFavourites(docRootElement));

		} catch (Exception e) {
			logger.error("Failed midway through creating user object from XML",
					e);
		}

		return (user);
	}
}
