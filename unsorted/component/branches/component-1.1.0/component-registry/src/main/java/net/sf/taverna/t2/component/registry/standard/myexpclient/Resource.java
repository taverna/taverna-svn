package net.sf.taverna.t2.component.registry.standard.myexpclient;

// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University

import static java.lang.String.format;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient.parseDate;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class Resource implements Comparable<Resource>, Serializable {
	private static final long serialVersionUID = -4529311100092445437L;

	// CONSTANTS
	// (resource types)
	public static enum Type {
		WORKFLOW(10, "Workflow"), FILE(11, "File"), PACK(12, "Pack"), INTERNAL(
				14, "Internal item"), EXTERNAL(15, "External item"), USER(20,
				"User"), GROUP(21, "Group"), TAG(30, "Tag"), UNKNOWN(0,
				"Unknown"), ERROR(-1, "ERROR: Unexpected unknown type!");
		private int value;
		private String name;

		private Type(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
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
			return Type.UNKNOWN;
		}

	}

	// (integer access types)
	public static enum Access {
		VIEWING(1), DOWNLOADING(2), EDITING(3);
		private int value;

		private Access(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	// (categories for selecting required elements for every resource type for a
	// particular purpose)
	public static enum RequestType {
		/**
		 * essentially obtains all data that API provides
		 */
		ALL(5000),
		/**
		 * used to get all data for preview in a browser window
		 */
		PREVIEW(5005),
		/**
		 * used for displaying results of searches by query / by tag
		 */
		FULL_LISTING(5010),
		/**
		 * used for displaying items in 'My Stuff' tab
		 */
		SHORT_LISTING(5015),
		/**
		 * Just the favourites
		 */
		FAVOURITES(5050),
		/**
		 * Just the tags.
		 */
		TAGS(5051),
		/**
		 * Just the workflow content.
		 */
		CONTENT(5055),
		/**
		 * used when default fields that come from the API are acceptable
		 */
		DEFAULT(5100);
		private int code;

		private RequestType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
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

	public Resource() {
		// empty constructor
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

	@Override
	public String toString() {
		return "(" + getItemTypeName() + ", " + getURI() + "," + getTitle()
				+ ")";
	}

	/**
	 * This method is needed to sort Resource instances.
	 */
	public int compareTo(Resource other) {
		int itemTypesCompared = getItemType().getValue()
				- other.getItemType().getValue();

		if (itemTypesCompared == 0)
			// types are identical, compare by title
			return getTitle().compareTo(other.getTitle());

		/*
		 * types are different - this is sufficient to order these two resources
		 * (NB! This presumes that type constants were set in a way that
		 * produces correct ordering of the types for sorting operations!)
		 */
		return itemTypesCompared;
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
		Access accessType;

		switch (itemType) {
		case WORKFLOW:
			accessType = ((Workflow) this).getAccessType();
			break;
		case FILE:
			accessType = ((File) this).getAccessType();
			break;
		case PACK:
			accessType = ((Pack) this).getAccessType();
			break;
		default:
			return false;
		}

		return accessType.value() >= Access.DOWNLOADING.value();
	}

	/**
	 * Only workflows (and files?) have visible types.
	 */
	public boolean hasVisibleType() {
		return (itemType == Type.WORKFLOW || itemType == Type.FILE);
	}

	public String getVisibleType() {
		switch (itemType) {
		case WORKFLOW:
			return ((Workflow) this).getVisibleType();
		case FILE:
			return ((File) this).getVisibleType();
		default:
			return null;
		}
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
