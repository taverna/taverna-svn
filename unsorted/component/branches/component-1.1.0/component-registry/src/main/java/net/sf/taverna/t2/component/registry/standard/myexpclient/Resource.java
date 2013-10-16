package net.sf.taverna.t2.component.registry.standard.myexpclient;

// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University

import static net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient.parseDate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class Resource implements Comparable<Resource>, Serializable {
	private static final long serialVersionUID = -4529311100092445437L;
	// CONSTANTS
	// (integer resource types)
	public static final int UNEXPECTED_TYPE = -1; // erroneous type
	public static final int UNKNOWN = 0;
	public static final int WORKFLOW = 10;
	public static final int FILE = 11;
	public static final int PACK = 12;
	public static final int PACK_INTERNAL_ITEM = 14;
	public static final int PACK_EXTERNAL_ITEM = 15;
	public static final int USER = 20;
	public static final int GROUP = 21;
	public static final int TAG = 30;
	public static final int COMMENT = 31;

	// (string resource types)
	public static final String WORKFLOW_VISIBLE_NAME = "Workflow";
	public static final String FILE_VISIBLE_NAME = "File";
	public static final String PACK_VISIBLE_NAME = "Pack";
	public static final String USER_VISIBLE_NAME = "User";
	public static final String GROUP_VISIBLE_NAME = "Group";
	public static final String TAG_VISIBLE_NAME = "Tag";
	public static final String COMMENT_VISIBLE_NAME = "Comment";
	public static final String UNKWNOWN_VISIBLE_NAME = "Unknown";
	public static final String UNEXPECTED_TYPE_VISIBLE_NAME = "ERROR: Unexpected unknown type!";

	// (integer access types)
	public static final int ACCESS_VIEWING = 1000;
	public static final int ACCESS_DOWNLOADING = 1001;
	public static final int ACCESS_EDITING = 1002;

	// (categories for selecting required elements for every resource type for a
	// particular purpose)
	/**
	 * essentially obtains all data that API provides
	 */
	public static final int REQUEST_ALL_DATA = 5000;
	/**
	 * used to get all data for preview in a browser window
	 */
	public static final int REQUEST_FULL_PREVIEW = 5005;
	/**
	 * used for displaying results of searches by query / by tag
	 */
	public static final int REQUEST_FULL_LISTING = 5010;
	/**
	 * used for displaying items in 'My Stuff' tab
	 */
	public static final int REQUEST_SHORT_LISTING = 5015;
	public static final int REQUEST_USER_FAVOURITES_ONLY = 5050;
	public static final int REQUEST_USER_APPLIED_TAGS_ONLY = 5051;
	public static final int REQUEST_WORKFLOW_CONTENT_ONLY = 5055;
	/**
	 * used when default fields that come from the API are acceptable
	 */
	public static final int REQUEST_DEFAULT_FROM_API = 5100;
	// instance variables
	private int iID;
	private String uri;
	private String resource;
	private String title;

	private int itemType;

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

	public String getURI() {
		return uri;
	}

	public String getResource() {
		return resource;
	}

	public int getItemType() {
		return itemType;
	}

	public String getItemTypeName() {
		return getResourceTypeName(itemType);
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

	public void setItemType(int type) {
		this.itemType = type;
	}

	public void setItemType(String type) {
		this.itemType = getResourceTypeFromVisibleName(type);
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
		int itemTypesCompared = this.getItemType() - other.getItemType();

		if (itemTypesCompared == 0) {
			// types are identical, compare by title
			return getTitle().compareTo(other.getTitle());
		}
		// types are different - this is sufficient to order these two
		// resources
		// (NB! This presumes that type constants were set in a way that
		// produces correct
		// ordering of the types for sorting operations!)
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

		// 'other' object is a Resource; equality is based on the data stored
		// in the current and 'other' Resource instances - the main data of the
		// Resource: item type, URI in the API and resource URL on myExperiment
		// (these fields will always be present in every Resource instance)
		Resource otherRes = (Resource) other;
		return itemType == otherRes.itemType && uri.equals(otherRes.uri)
				&& resource.equals(otherRes.resource);
	}

	/**
	 * Check if the current type of resource is supposed to have an uploader.
	 */
	public boolean hasUploader() {
		return (itemType == WORKFLOW || itemType == FILE);
	}

	/**
	 * Casts the resource to one of the specialist types to get the uploader.
	 */
	public User getUploader() {
		switch (itemType) {
		case WORKFLOW:
			return ((Workflow) this).getUploader();
		case FILE:
			return ((File) this).getUploader();
		default:
			return null;
		}
	}

	/**
	 * Check if the current type of resource is supposed to have a creator.
	 */
	public boolean hasCreator() {
		return (itemType == PACK);
	}

	/**
	 * Casts the resource to one of the specialist types to get the creator.
	 */
	public User getCreator() {
		switch (itemType) {
		case PACK:
			return ((Pack) this).getCreator();
		default:
			return null;
		}
	}

	/**
	 * Check if the current type of resource is supposed to have a
	 * administrator.
	 */
	public boolean hasAdmin() {
		return (itemType == GROUP);
	}

	/**
	 * Casts the resource to one of the specialist types to get the
	 * administrator.
	 */
	public User getAdmin() {
		switch (itemType) {
		case GROUP:
			return ((Group) this).getAdmin();
		default:
			return null;
		}
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
	 * Determines whether the current resource is favourited by specified user.
	 */
	public boolean isFavouritedBy(User user) {
		for (Resource r : user.getFavourites())
			if (r.getURI().equals(getURI()))
				return true;
		return false;
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
			return (true);
		default:
			return (false);
		}
	}

	/**
	 * Retrieves the collection of comments for the current resource.
	 */
	public List<Comment> getComments() {
		switch (itemType) {
		case WORKFLOW:
			return ((Workflow) this).getComments();
		case FILE:
			return ((File) this).getComments();
		case PACK:
			return ((Pack) this).getComments();
		case GROUP:
			return ((Group) this).getComments();
		default:
			return (null);
		}
	}

	/**
	 * Determines whether the current type of resource can be downloaded in
	 * general.
	 */
	public boolean isDownloadable() {
		return (itemType == WORKFLOW || itemType == FILE || itemType == PACK);
	}

	/**
	 * Determines whether the current resource instance can be downloaded by the
	 * current user.
	 */
	public boolean isDownloadAllowed() {
		int iAccessType = 0;

		switch (itemType) {
		case WORKFLOW:
			iAccessType = ((Workflow) this).getAccessType();
			break;
		case FILE:
			iAccessType = ((File) this).getAccessType();
			break;
		case PACK:
			iAccessType = ((Pack) this).getAccessType();
			break;
		default:
			iAccessType = 0;
		}

		return (iAccessType >= ACCESS_DOWNLOADING);
	}

	/**
	 * Only workflows (and files?) have visible types.
	 */
	public boolean hasVisibleType() {
		return (itemType == WORKFLOW || itemType == FILE);
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
	 * Translates resource type codes into a textual representation.
	 * 
	 * @param resourceTypeCode
	 *            This code should be one of the resource type constants defined
	 *            in Resource class.
	 * @return Textual translation of the resource type code.
	 */
	public static String getResourceTypeName(int resourceTypeCode) {
		switch (resourceTypeCode) {
		case WORKFLOW:
			return WORKFLOW_VISIBLE_NAME;
		case FILE:
			return FILE_VISIBLE_NAME;
		case PACK:
			return PACK_VISIBLE_NAME;
		case USER:
			return USER_VISIBLE_NAME;
		case GROUP:
			return GROUP_VISIBLE_NAME;
		case TAG:
			return TAG_VISIBLE_NAME;
		case COMMENT:
			return COMMENT_VISIBLE_NAME;
		case UNKNOWN:
			return UNKWNOWN_VISIBLE_NAME;
		default:
			return UNEXPECTED_TYPE_VISIBLE_NAME;
		}
	}

	/**
	 * Translates resource visible name into the type codes.
	 */
	public static int getResourceTypeFromVisibleName(String name) {
		if (name.toLowerCase().equals(WORKFLOW_VISIBLE_NAME.toLowerCase()))
			return WORKFLOW;
		else if (name.toLowerCase().equals(FILE_VISIBLE_NAME.toLowerCase()))
			return FILE;
		else if (name.toLowerCase().equals(PACK_VISIBLE_NAME.toLowerCase()))
			return PACK;
		else if (name.toLowerCase().equals(USER_VISIBLE_NAME.toLowerCase()))
			return USER;
		else if (name.toLowerCase().equals(GROUP_VISIBLE_NAME.toLowerCase()))
			return GROUP;
		else if (name.toLowerCase().equals(TAG_VISIBLE_NAME.toLowerCase()))
			return TAG;
		else if (name.toLowerCase().equals(COMMENT_VISIBLE_NAME.toLowerCase()))
			return COMMENT;
		else
			return UNKNOWN;
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
		switch (getResourceTypeFromVisibleName(docRootElement.getLocalName())) {
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
