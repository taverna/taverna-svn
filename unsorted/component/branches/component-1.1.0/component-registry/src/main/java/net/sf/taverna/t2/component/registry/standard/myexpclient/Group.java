/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import static java.util.Collections.sort;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient.parseDate;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeResource;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveTags;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Sergejs Aleksejevs
 */
public class Group extends Resource {
	private static final long serialVersionUID = -1115193144192642722L;

	private User admin;

	private List<Tag> tags;
	private List<User> members;
	private List<Resource> sharedItems;

	public Group() {
		super();
		this.setItemType(Type.GROUP);
	}

	@Override
	public User getAdmin() {
		return admin;
	}

	public void setAdmin(User admin) {
		this.admin = admin;
	}

	public List<Tag> getTags() {
		return this.tags;
	}

	public int getSharedItemCount() {
		return this.sharedItems.size();
	}

	public int getMemberCount() {
		return this.members.size();
	}

	public List<Resource> getSharedItems() {
		return this.sharedItems;
	}

	public List<User> getMembers() {
		return this.members;
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
		String elements = "";

		/*
		 * cases higher up in the list are supersets of those that come below -
		 * hence no "break" statements are required, because 'falling through'
		 * the switch statement is the desired behaviour in this case
		 */
		switch (requestType) {
		case PREVIEW:
			elements += "created-at,updated-at,members,shared-items,tags,comments,";
		case FULL_LISTING:
			elements += "owner,";
		case SHORT_LISTING:
			elements += "id,title,description";
		}

		return elements;
	}

	public static Group buildFromXML(Document doc, Logger logger) {
		// if no XML document was supplied, return NULL
		if (doc == null)
			return null;

		// call main method which parses XML document starting from root element
		return buildFromXML(doc.getDocumentElement(), logger);
	}

	// class method to build a group instance from XML
	public static Group buildFromXML(Element docRootElement, Logger logger) {
		// return null to indicate an error if XML document contains no root
		// element
		if (docRootElement == null)
			return (null);

		Group g = new Group();

		try {
			// URI
			g.setURI(docRootElement.getAttribute("uri"));

			// Resource URI
			g.setResource(docRootElement.getAttribute("resource"));

			// Id
			String id = getChildText(docRootElement, "id");
			if (id == null || id.equals("")) {
				id = "API Error - No group ID supplied";
				logger.error("Error while parsing group XML data - no ID provided for group with title: \""
						+ getChildText(docRootElement, "title") + "\"");
			}
			g.setID(Integer.parseInt(id));

			// Title
			g.setTitle(getChildText(docRootElement, "title"));

			// Description
			g.setDescription(getChildText(docRootElement, "description"));

			// Owner
			g.setAdmin(makeUser(getChild(docRootElement, "owner")));

			// Created at
			String createdAt = getChildText(docRootElement, "created-at");
			if (createdAt != null && !createdAt.equals(""))
				g.setCreatedAt(parseDate(createdAt));

			// Updated at
			String updatedAt = getChildText(docRootElement, "updated-at");
			if (updatedAt != null && !updatedAt.equals(""))
				g.setUpdatedAt(parseDate(updatedAt));

			// Tags
			g.tags = retrieveTags(docRootElement);

			// Members
			g.members = new ArrayList<User>();
			Element membersElement = getChild(docRootElement, "members");
			if (membersElement != null) {
				NodeList memberNodes = membersElement.getChildNodes();
				for (int i = 0; i < memberNodes.getLength(); i++) {
					Element e = (Element) memberNodes.item(i);
					g.members.add(makeUser(e));
				}
			}
			// sort the items after all items have been added
			sort(g.members);

			// Shared Items
			g.sharedItems = new ArrayList<Resource>();
			Element sharedItemsElement = getChild(docRootElement,
					"shared-items");
			if (sharedItemsElement != null) {
				NodeList itemsNodes = sharedItemsElement.getChildNodes();
				for (int i = 0; i < itemsNodes.getLength(); i++) {
					Element e = (Element) itemsNodes.item(i);
					g.sharedItems.add(makeResource(e));
				}
			}
			// sort the items after all items have been added
			sort(g.sharedItems);

			logger.debug("Found information for group with ID: " + g.getID()
					+ ", Title: " + g.getTitle());
		} catch (Exception e) {
			logger.error(
					"Failed midway through creating group object from XML", e);
		}

		// return created group instance
		return g;
	}
}
