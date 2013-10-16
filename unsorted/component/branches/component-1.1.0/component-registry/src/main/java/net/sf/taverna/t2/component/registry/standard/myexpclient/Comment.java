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

import static net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient.parseDate;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class Comment extends Resource {
	private static final long serialVersionUID = 5564200571354269958L;

	private User user;
	private String comment;
	private int typeOfCommentedResource;
	private String uriOfCommentedResource;

	public Comment() {
		super();
		setItemType(COMMENT);
	}

	public Comment(Document doc, Logger logger) {
		this();
		Element root = doc.getDocumentElement();

		setResource(root.getAttribute("resource"));
		setURI(root.getAttribute("uri"));

		setTitle(comment = getChildText(root, "comment"));

		Element commentedResource = getChild(root, "subject");
		if (commentedResource != null) {
			typeOfCommentedResource = getResourceTypeFromVisibleName(commentedResource
					.getTagName());
			uriOfCommentedResource = commentedResource.getAttribute("uri");
		}

		user = makeUser(getChild(root, "author"));

		String createdAt = getChildText(root, "created-at");
		if (createdAt != null && !createdAt.isEmpty())
			setCreatedAt(createdAt);

		logger.debug("Found information for comment with ID: " + getID()
				+ ", URI: " + getURI());
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getTypeOfCommentedResource() {
		return typeOfCommentedResource;
	}

	public void setTypeOfCommentedResource(int typeOfCommentedResource) {
		this.typeOfCommentedResource = typeOfCommentedResource;
	}

	public String getURIOfCommentedResource() {
		return uriOfCommentedResource;
	}

	public void setURIOfCommentedResource(String uriOfCommetedResource) {
		this.uriOfCommentedResource = uriOfCommetedResource;
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
	public static String getRequiredAPIElements(int requestType) {
		String elements = "";

		// cases higher up in the list are supersets of those that come below -
		// hence no "break" statements are required, because 'falling through'
		// the
		// switch statement is the desired behaviour in this case
		switch (requestType) {
		case REQUEST_DEFAULT_FROM_API:
			elements += ""; // no change needed - defaults will be used
		}

		return elements;
	}

	// class method to build a comment instance from XML
	public static Comment buildFromXML(Document doc, Logger logger) {
		// if no XML was supplied, return null to indicate an error
		if (doc == null)
			return null;

		Comment c = new Comment();

		try {
			Element root = doc.getDocumentElement();

			c.setResource(root.getAttribute("resource"));
			c.setURI(root.getAttribute("uri"));

			c.setTitle(getChildText(root, "comment"));
			c.setComment(getChildText(root, "comment"));

			Element commentedResource = getChild(root, "subject");
			if (commentedResource != null) {
				c.setTypeOfCommentedResource(getResourceTypeFromVisibleName(commentedResource
						.getTagName()));
				c.setURIOfCommentedResource(commentedResource
						.getAttribute("uri"));
			}

			Element userElement = getChild(root, "author");
			c.setUser(makeUser(userElement));

			String createdAt = getChildText(root, "created-at");
			if (createdAt != null && !createdAt.equals(""))
				c.setCreatedAt(parseDate(createdAt));

			logger.debug("Found information for comment with ID: " + c.getID()
					+ ", URI: " + c.getURI());
		} catch (Exception e) {
			logger.error(
					"Failed midway through creating comment object from XML", e);
		}

		// return created comment instance
		return c;
	}

}
