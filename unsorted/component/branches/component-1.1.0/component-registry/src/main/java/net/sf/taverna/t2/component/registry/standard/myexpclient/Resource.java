package net.sf.taverna.t2.component.registry.standard.myexpclient;

// Copyright (C) 2008-2013 The University of Manchester, University of Southampton
// and Cardiff University

import static java.lang.String.format;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Access.DOWNLOADING;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.children;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.parseDate;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public abstract class Resource implements Comparable<Resource>, Serializable {
	// CONSTANTS
	// (resource types)
	public static enum Type {
		ERROR("ERROR: Unexpected unknown type!"), UNKNOWN("Unknown"), WORKFLOW(
				"Workflow"), FILE("File"), PACK("Pack"), INTERNAL(
				"Internal item"), EXTERNAL("External item"), USER("User"), GROUP(
				"Group"), TAG("Tag");
		private String name;

		private Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		private String getLower() {
			return name.toLowerCase();
		}

		/**
		 * Translates resource visible name into the type codes.
		 */
		public static Type forName(String name) {
			for (Type t : values())
				if (t.getLower().equals(name.toLowerCase()))
					return t;
			return UNKNOWN;
		}
	}

	// (integer access types)
	public static enum Access {
		VIEWING, DOWNLOADING, EDITING;
		/**
		 * Takes XML Element instance with privilege listing for an item and
		 * returns an integer value for that access type.
		 */
		public static Access access(Element privileges) {
			if (privileges == null)
				return Access.VIEWING;

			/*
			 * if the item for which the privileges are processed got received,
			 * there definitely is viewing access to it. We need to pick the
			 * highest level of access assigned.
			 */
			Access accessType = Access.VIEWING;

			for (Element privilege : children(privileges, "privilege")) {
				String strValue = privilege.getAttribute("type");

				Access thisPrivilege = Access.VIEWING;
				if (strValue.equals("download"))
					thisPrivilege = Access.DOWNLOADING;
				else if (strValue.equals("edit"))
					thisPrivilege = Access.EDITING;

				if (thisPrivilege.compareTo(accessType) > 0)
					accessType = thisPrivilege;
			}

			return accessType;
		}

	}

	// (categories for selecting required elements for every resource type for a
	// particular purpose)
	public static enum RequestType {
		/**
		 * essentially obtains all data that API provides
		 */
		ALL,
		/**
		 * used to get all data for preview in a browser window
		 */
		PREVIEW,
		/**
		 * used for displaying results of searches by query / by tag
		 */
		FULL_LISTING,
		/**
		 * used for displaying items in 'My Stuff' tab
		 */
		SHORT_LISTING,
		/**
		 * Just the favourites
		 */
		FAVOURITES,
		/**
		 * Just the tags.
		 */
		TAGS,
		/**
		 * Just the workflow content.
		 */
		CONTENT,
		/**
		 * used when default fields that come from the API are acceptable
		 */
		DEFAULT
	}

	/**
	 * @author Jiten Bhagat, Emmanuel Tagarira
	 */
	public enum License {
		BY_ND("by-nd", "Creative Commons Attribution-NoDerivs 3.0 License",
				"http://creativecommons.org/licenses/by-nd/3.0/"), BY("by",
				"Creative Commons Attribution 3.0 License",
				"http://creativecommons.org/licenses/by/3.0/"), BY_SA("by-sa",
				"Creative Commons Attribution-Share Alike 3.0 License",
				"http://creativecommons.org/licenses/by-sa/3.0/"), BY_NC_ND(
				"by-nc-nd",
				"Creative Commons Attribution-Noncommercial-NoDerivs 3.0 License",
				"http://creativecommons.org/licenses/by-nc-nd/3.0/"), BY_NC(
				"by-nc",
				"Creative Commons Attribution-Noncommercial 3.0 License",
				"http://creativecommons.org/licenses/by-nc/3.0/"), BY_NC_SA(
				"by-nc-sa",
				"Creative Commons Attribution-Noncommercial-Share Alike 3.0 License",
				"http://creativecommons.org/licenses/by-nc-sa/3.0/");
		private String type;
		private String text;
		private String link;
		public static License[] SUPPORTED_TYPES = { BY_ND, BY, BY_SA, BY_NC_ND,
				BY_NC, BY_NC_SA };
		public static License DEFAULT_LICENSE = BY_SA;

		private License(String type, String text, String link) {
			this.type = type;
			this.text = text;
			this.link = link;
		}

		public String getType() {
			return type;
		}

		public String getText() {
			return text;
		}

		public String getLink() {
			return link;
		}

		public static License getInstance(String type) {
			if (type == null)
				return null;

			if (type.equalsIgnoreCase("by-nd")) {
				return BY_ND;
			} else if (type.equalsIgnoreCase("by")) {
				return BY;
			} else if (type.equalsIgnoreCase("by-sa")) {
				return BY_SA;
			} else if (type.equalsIgnoreCase("by-nc-nd")) {
				return BY_NC_ND;
			} else if (type.equalsIgnoreCase("by-nc")) {
				return BY_NC;
			} else if (type.equalsIgnoreCase("by-nc-sa")) {
				return BY_NC_SA;
			} else {
				return null;
			}
		}
	}

	static class Cache<T extends Resource> {
		private final WeakHashMap<String, WeakReference<T>> cache = new WeakHashMap<String, WeakReference<T>>();

		public synchronized void put(T item) {
			cache.put(item.getURI(), new WeakReference<T>(item));
		}

		public synchronized T get(Element element) {
			return cache.get(element.getAttribute("uri")).get();
		}
	}

	// instance variables
	private int iID;
	private String uri;
	private String resource;
	private String title;
	private Type itemType;
	private Date createdAt;
	private Date updatedAt;
	private String description;

	public Resource(Type type) {
		itemType = type;
	}

	protected Resource(Type type, Element docRootElement, Logger logger) {
		itemType = type;
		setURI(docRootElement.getAttribute("uri"));
		setResource(docRootElement.getAttribute("resource"));
		setID(docRootElement, logger);
		setTitle(getChildText(docRootElement, "title"));
		setDescription(getChildText(docRootElement, "description"));
	}

	public int getID() {
		return iID;
	}

	public void setID(int id) {
		this.iID = id;
	}

	public void setID(String id) {
		this.iID = Integer.parseInt(id);
	}

	protected void setID(Element element, Logger logger) {
		String id = element.getAttribute("id");
		if (id == null || id.isEmpty())
			id = getChildText(element, "id");
		if (id == null || id.isEmpty()) {
			id = "API Error - No file ID supplied";
			String message = format("Error while parsing workflow XML data:"
					+ " no ID provided for workflow with title: \"%s\"",
					getChildText(element, "title"));
			if (logger == null)
				throw new RuntimeException(message);
			logger.error(message);
		}
		setID(id);
	}

	public String getURI() {
		return uri;
	}

	public String getResource() {
		return resource;
	}

	public Type getItemType() {
		return itemType;
	}

	public String getItemTypeName() {
		return itemType.getName();
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setItemType(Type type) {
		this.itemType = type;
	}

	public void setItemType(String type) {
		this.itemType = Type.forName(type);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = parseDate(createdAt);
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = parseDate(updatedAt);
	}

	protected Access getAccessType() {
		return Access.VIEWING;
	}

	@Override
	public String toString() {
		return "(" + getItemTypeName() + ", " + getURI() + "," + getTitle()
				+ ")";
	}

	/**
	 * This method is needed to sort Resource instances.
	 */
	@Override
	public int compareTo(Resource other) {
		int itemTypesCompared = getItemType().compareTo(other.getItemType());
		if (itemTypesCompared != 0)
			/*
			 * types are different - this is sufficient to order these two
			 * resources (NB! This presumes that type constants were set in a
			 * way that produces correct ordering of the types for sorting
			 * operations!)
			 */
			return itemTypesCompared;

		// types are identical, compare by title
		return getTitle().compareTo(other.getTitle());
	}

	/**
	 * This makes sure that things like instanceOf() and remove() in List
	 * interface work properly - this way resources are treated to be the same
	 * if they store identical data, rather than they simply hold the same
	 * reference.
	 */
	@Override
	public boolean equals(Object other) {
		// could only be equal to another Resource object, not anything else
		if (!(other instanceof Resource))
			return false;

		/*
		 * 'other' object is a Resource; equality is based on the data stored in
		 * the current and 'other' Resource instances - the main data of the
		 * Resource: item type, URI in the API and resource URL on myExperiment
		 * (these fields will always be present in every Resource instance)
		 */
		Resource otherRes = (Resource) other;
		return itemType == otherRes.itemType && uri.equals(otherRes.uri)
				&& resource.equals(otherRes.resource);
	}

	@Override
	public int hashCode() {
		return itemType.hashCode() ^ uri.hashCode() ^ resource.hashCode();
	}

	/**
	 * Check if the current type of resource is supposed to have an uploader.
	 */
	public boolean hasUploader() {
		return (itemType == Type.WORKFLOW || itemType == Type.FILE);
	}

	/**
	 * Casts the resource to one of the specialist types to get the uploader.
	 */
	public User getUploader() {
		return null;
	}

	/**
	 * Check if the current type of resource is supposed to have a creator.
	 */
	public boolean hasCreator() {
		return (itemType == Type.PACK);
	}

	/**
	 * Casts the resource to one of the specialist types to get the creator.
	 */
	public User getCreator() {
		return null;
	}

	/**
	 * Check if the current type of resource is supposed to have a
	 * administrator.
	 */
	public boolean hasAdmin() {
		return (itemType == Type.GROUP);
	}

	/**
	 * Casts the resource to one of the specialist types to get the
	 * administrator.
	 */
	public User getAdmin() {
		return null;
	}

	/**
	 * Determines whether the current type of resource can be favourited.
	 */
	public boolean isFavouritable() {
		switch (this.itemType) {
		case WORKFLOW:
		case FILE:
		case PACK:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Determines whether the current type of resource can be commented on.
	 */
	public boolean isCommentableOn() {
		switch (itemType) {
		case WORKFLOW:
		case FILE:
		case PACK:
		case GROUP:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Determines whether the current type of resource can be downloaded in
	 * general.
	 */
	public boolean isDownloadable() {
		return (itemType == Type.WORKFLOW || itemType == Type.FILE || itemType == Type.PACK);
	}

	/**
	 * Determines whether the current resource instance can be downloaded by the
	 * current user.
	 */
	public boolean isDownloadAllowed() {
		switch (itemType) {
		case WORKFLOW:
		case FILE:
		case PACK:
			return getAccessType().compareTo(DOWNLOADING) >= 0;
		default:
			return false;
		}

	}

	/**
	 * Only workflows (and files?) have visible types.
	 */
	public boolean hasVisibleType() {
		return (itemType == Type.WORKFLOW || itemType == Type.FILE);
	}

	public String getVisibleType() {
		return null;
	}

	/**
	 * This method will act as dispatcher for local buildFromXML() methods for
	 * each of individual resource types. This way there's a generic way to turn
	 * XML content into a Resource instance.
	 */
	public static Resource buildFromXML(Document resourceXMLDocument,
			MyExperimentClient client, Logger logger) {
		return buildFromXML(resourceXMLDocument.getDocumentElement(), client,
				logger);
	}

	/**
	 * This method will act as dispatcher for local buildFromXML() methods for
	 * each of individual resource types. This way there's a generic way to turn
	 * XML content into a Resource instance.
	 */
	public static Resource buildFromXML(Element docRootElement,
			MyExperimentClient client, Logger logger) {
		switch (Type.forName(docRootElement.getLocalName())) {
		case WORKFLOW:
			return Workflow.buildFromXML(docRootElement, logger);
		case FILE:
			return File.buildFromXML(docRootElement, logger);
		case PACK:
			return Pack.buildFromXML(docRootElement, client, logger);
		case USER:
			return User.buildFromXML(docRootElement, logger);
		case GROUP:
			return Group.buildFromXML(docRootElement, logger);
		default:
			return null;
		}
	}

}
