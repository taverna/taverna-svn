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

import static java.lang.String.format;
import static java.util.Collections.sort;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Access.access;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.RequestType.DEFAULT;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.EXTERNAL;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.INTERNAL;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.PACK;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.children;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveTags;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class Pack extends Resource {
	private static final long serialVersionUID = -3977736206990103689L;

	private Access accessType;
	private User creator;
	private final List<Tag> tags = new ArrayList<Tag>();
	private final List<PackItem> items = new ArrayList<PackItem>();

	public Pack() {
		super(PACK);
	}

	Pack(Element root, Logger logger) {
		super(PACK, root, logger);
	}

	@Override
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
	public static Pack buildFromXML(Element root, MyExperimentClient client,
			Logger logger) {
		// return null to indicate an error if XML document contains no root
		// element
		if (root == null)
			return null;

		Pack p = new Pack();

		try {
			p = new Pack(root, logger);

			p.setAccessType(access(getChild(root, "privileges")));
			p.setCreator(makeUser(getChild(root, "owner")));
			p.tags.addAll(retrieveTags(root));

			// === All items will be stored together in one array ===
			// adding internal items first
			for (Element e : children(getChild(root, "internal-pack-items")))
				p.items.add(PackItem.buildFromXML(client.getResource(INTERNAL,
						e.getAttribute("uri"), DEFAULT), logger));
			// now adding external items
			for (Element e : children(getChild(root, "external-pack-items")))
				p.items.add(PackItem.buildFromXML(client.getResource(EXTERNAL,
						e.getAttribute("uri"), DEFAULT), logger));
			// sort the items after all of those have been added
			sort(p.items);

			logger.debug(format(
					"Found information for pack with ID: %s, Title: %s",
					p.getID(), p.getTitle()));
		} catch (Exception e) {
			logger.error("Failed midway through creating pack object from XML",
					e);
		}

		// return created pack instance
		return p;
	}
}
