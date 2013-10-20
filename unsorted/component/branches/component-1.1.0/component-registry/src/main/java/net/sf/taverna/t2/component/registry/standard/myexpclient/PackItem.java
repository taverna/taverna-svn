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

import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.EXTERNAL;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.UNKNOWN;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.children;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeResource;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class PackItem extends Resource {
	private static final long serialVersionUID = -4022813839129785667L;

	private User userWhoAddedThisItem;
	private String comment;
	private boolean internalItem;
	private Resource item; // for internal items
	private String link; // for external items
	private String alternateLink; // for external items

	public PackItem() {
		super(UNKNOWN);
		/*
		 * set to unknown originally; will be changed as soon as the type is
		 * known
		 */
	}
	PackItem(Element root, Logger logger) {
		super(UNKNOWN, root, logger);
		/*
		 * set to unknown originally; will be changed as soon as the type is
		 * known
		 */
	}

	public boolean isInternalItem() {
		return this.internalItem;
	}

	public void setInternalItem(boolean isInternalItem) {
		this.internalItem = isInternalItem;
	}

	public User getUserWhoAddedTheItem() {
		return this.userWhoAddedThisItem;
	}

	public void setUserWhoAddedTheItem(User userWhoAddedTheItem) {
		this.userWhoAddedThisItem = userWhoAddedTheItem;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Resource getItem() {
		return this.item;
	}

	public void setItem(Resource item) {
		this.item = item;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAlternateLink() {
		return this.alternateLink;
	}

	public void setAlternateLink(String alternateLink) {
		this.alternateLink = alternateLink;
	}

	public static PackItem buildFromXML(Document doc, Logger logger) {
		// if no XML was supplied, return null to indicate an error
		if (doc == null)
			return null;

		PackItem p = new PackItem();

		try {
			Element root = doc.getDocumentElement();
			p = new PackItem(root, logger);

			// User who added the item to the pack
			Element ownerElement = getChild(root, "owner");
			p.setUserWhoAddedTheItem(makeUser(ownerElement));

			// Comment
			Element commentElement = getChild(root, "comment");
			if (commentElement != null)
				p.setComment(commentElement.getTextContent());

			// === UP TO THIS POINT EXTERNAL AND INTERNAL ITEMS HAD THE SAME
			// DATA ===
			if (root.getTagName().equals("internal-pack-item")) {
				// record that this is internal item
				p.setInternalItem(true);

				// add a link to a resource for internal items
				try {
					p.setItem(makeResource(children(getChild(root, "item"))
							.get(0)));
				} catch (IndexOutOfBoundsException e) {
					// Do nothing
				}

				/*
				 * now need to replicate title and item type attributes to the
				 * pack item object itself - this is required to allow proper
				 * sorting of the items
				 */
				p.setItemType(p.getItem().getItemType());
				p.setTitle(p.getItem().getTitle());
			} else {
				// record that this is external item
				p.setInternalItem(false);

				// add links to the external resource for external items
				p.setItemType(EXTERNAL);
				p.setTitle(getChildText(root, "title"));
				p.setLink(getChildText(root, "uri"));
				p.setAlternateLink(getChildText(root, "alternate-uri"));
			}

			logger.debug("Found information for pack item with URI: "
					+ p.getURI());
		} catch (Exception e) {
			logger.error(
					"Failed midway through creating pack item object from XML",
					e);
		}

		// return created pack item instance
		return p;
	}
}
