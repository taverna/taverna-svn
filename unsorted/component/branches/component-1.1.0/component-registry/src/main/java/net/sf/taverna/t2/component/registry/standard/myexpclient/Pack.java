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
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Sergejs Aleksejevs
 */
public class Pack extends Resource {
	private static final long serialVersionUID = -3977736206990103689L;

	private Access accessType;
	private User creator;
	private List<Tag> tags;
	private List<PackItem> items;

	public Pack() {
		super();
		this.setItemType(Type.PACK);
	}

	public Access getAccessType() {
		return accessType;
	}

	public void setAccessType(Access accessType) {
		this.accessType = accessType;
	}

	@Override
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public List<Tag> getTags() {
		return this.tags;
	}

	public int getItemCount() {
		return this.items.size();
	}

	public List<PackItem> getItems() {
		return this.items;
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
			elements += "created-at,updated-at,internal-pack-items,external-pack-items,tags,comments,";
		case FULL_LISTING:
			elements += "owner,";
		case SHORT_LISTING:
			elements += "id,title,description,privileges";
		}

		return elements;
	}

	public static Pack buildFromXML(Document doc, MyExperimentClient client,
			Logger logger) {
		// if no XML document was supplied, return NULL
		if (doc == null)
			return null;

		// call main method which parses XML document starting from root element
		return buildFromXML(doc.getDocumentElement(), client, logger);
	}

	// class method to build a pack instance from XML
	public static Pack buildFromXML(Element docRootElement,
			MyExperimentClient client, Logger logger) {
		// return null to indicate an error if XML document contains no root
		// element
		if (docRootElement == null)
			return (null);

		Pack p = new Pack();

		try {
			// Access type
			p.setAccessType(Util.getAccessType(getChild(docRootElement,
					"privileges")));

			// URI
			p.setURI(docRootElement.getAttribute("uri"));

			// Resource URI
			p.setResource(docRootElement.getAttribute("resource"));

			// Id
			String id = getChildText(docRootElement, "id");
			if (id == null || id.equals("")) {
				id = "API Error - No pack ID supplied";
				logger.error("Error while parsing pack XML data - no ID provided for pack with title: \""
						+ getChildText(docRootElement, "title") + "\"");
			}
			p.setID(Integer.parseInt(id));

			// Title
			p.setTitle(getChildText(docRootElement, "title"));

			// Description
			p.setDescription(getChildText(docRootElement, "description"));

			// Owner
			p.setCreator(Util.makeUser(getChild(docRootElement, "owner")));

			// Created at
			String createdAt = getChildText(docRootElement, "created-at");
			if (createdAt != null && !createdAt.equals("")) {
				p.setCreatedAt(MyExperimentClient.parseDate(createdAt));
			}

			// Updated at
			String updatedAt = getChildText(docRootElement, "updated-at");
			if (updatedAt != null && !updatedAt.equals("")) {
				p.setUpdatedAt(MyExperimentClient.parseDate(updatedAt));
			}

			// Tags
			p.tags = new ArrayList<Tag>();
			p.getTags().addAll(Util.retrieveTags(docRootElement));

			// === All items will be stored together in one array ===
			p.items = new ArrayList<PackItem>();
			// adding internal items first
			Element itemsElement = getChild(docRootElement,
					"internal-pack-items");
			if (itemsElement != null) {
				NodeList itemsNodes = itemsElement.getChildNodes();
				for (int i = 0; i < itemsNodes.getLength(); i++) {
					Element e = (Element) itemsNodes.item(i);
					Document docCurrentItem = client.getResource(Type.INTERNAL,
							e.getAttribute("uri"), RequestType.DEFAULT);
					PackItem piCurrentItem = PackItem.buildFromXML(
							docCurrentItem, logger);

					p.getItems().add(piCurrentItem);
				}
			}

			// now adding external items
			itemsElement = getChild(docRootElement, "external-pack-items");
			if (itemsElement != null) {
				NodeList itemsNodes = itemsElement.getChildNodes();
				for (int i = 0; i < itemsNodes.getLength(); i++) {
					Element e = (Element) itemsNodes.item(i);
					Document docCurrentItem = client.getResource(Type.EXTERNAL,
							e.getAttribute("uri"), RequestType.DEFAULT);
					p.items.add(PackItem.buildFromXML(docCurrentItem, logger));
				}
			}

			// sort the items after all of those have been added
			sort(p.items);

			logger.debug("Found information for pack with ID: " + p.getID()
					+ ", Title: " + p.getTitle());
		} catch (Exception e) {
			logger.error("Failed midway through creating pack object from XML",
					e);
		}

		// return created pack instance
		return (p);
	}
}
