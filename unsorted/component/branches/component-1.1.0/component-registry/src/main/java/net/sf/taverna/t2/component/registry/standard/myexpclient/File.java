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
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveAttributions;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveComments;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveCredits;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveTags;

import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class File extends Resource {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9051574197214290716L;

	private int accessType;

	private User uploader;
	private License license;
	private String filename;
	private String visibleType;
	private String contentType;
	private List<Tag> tags;
	private List<Comment> comments;
	private List<Resource> credits;
	private List<Resource> attributions;

	public File() {
		super();
		this.setItemType(FILE);
	}

	public int getAccessType() {
		return this.accessType;
	}

	public void setAccessType(int accessType) {
		this.accessType = accessType;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public User getUploader() {
		return uploader;
	}

	public void setUploader(User uploader) {
		this.uploader = uploader;
	}

	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getVisibleType() {
		return this.visibleType;
	}

	public void setVisibleType(String visibleType) {
		this.visibleType = visibleType;
	}

	public List<Comment> getComments() {
		return this.comments;
	}

	public List<Resource> getCredits() {
		return this.credits;
	}

	public List<Resource> getAttributions() {
		return this.attributions;
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
		String elements = "";

		// cases higher up in the list are supersets of those that come below -
		// hence no "break" statements are required, because 'falling through'
		// the
		// switch statement is the desired behaviour in this case
		switch (iRequestType) {
		case REQUEST_FULL_PREVIEW:
			elements += "filename,content-type,created-at,updated-at,"
					+ "license-type,tags,comments,credits,attributions,";
		case REQUEST_FULL_LISTING:
			elements += "uploader,type,";
		case REQUEST_SHORT_LISTING:
			elements += "id,title,description,privileges";
		}

		return elements;
	}

	public static File buildFromXML(Document doc, Logger logger) {
		// if no XML document was supplied, return NULL
		if (doc == null)
			return null;

		// call main method which parses XML document starting from root element
		return buildFromXML(doc.getDocumentElement(), logger);
	}

	// class method to build a file instance from XML
	public static File buildFromXML(Element docRootElement, Logger logger) {
		// return null to indicate an error if XML document contains no root
		// element
		if (docRootElement == null)
			return null;

		File f = new File();

		try {
			// Access type
			Element privs = (Element) docRootElement.getElementsByTagName(
					"privileges").item(0);
			f.setAccessType(Util.getAccessType(privs));

			// URI
			f.setURI(docRootElement.getAttribute("uri"));

			// Resource URI
			f.setResource(docRootElement.getAttribute("resource"));

			// Id
			String id = getChildText(docRootElement, "id");
			if (id == null || id.equals("")) {
				id = "API Error - No file ID supplied";
				logger.error("Error while parsing file XML data - no ID provided for file with title: \""
						+ getChildText(docRootElement, "title") + "\"");
			}
			f.setID(id);

			// Filename
			f.setFilename(getChildText(docRootElement, "filename"));

			// Title
			f.setTitle(getChildText(docRootElement, "title"));

			// Description
			f.setDescription(getChildText(docRootElement, "description"));

			// Uploader
			Element uploaderElement = (Element) docRootElement
					.getElementsByTagName("uploader").item(0);
			f.setUploader(makeUser(uploaderElement));

			// Created at
			String createdAt = getChildText(docRootElement, "created-at");
			if (createdAt != null && !createdAt.equals("")) {
				f.setCreatedAt(parseDate(createdAt));
			}

			// Updated at
			String updatedAt = getChildText(docRootElement, "updated-at");
			if (updatedAt != null && !updatedAt.equals("")) {
				f.setUpdatedAt(parseDate(updatedAt));
			}

			// License
			f.setLicense(License.getInstance(getChildText(docRootElement,
					"license-type")));

			// Type and Content-Type
			f.setVisibleType(getChildText(docRootElement, "type"));
			f.setContentType(getChildText(docRootElement, "content-type"));

			// Tags
			f.tags = retrieveTags(docRootElement);

			// Comments
			f.comments = retrieveComments(docRootElement, f);

			// Credits
			f.credits = retrieveCredits(docRootElement);

			// Attributions
			f.attributions = retrieveAttributions(docRootElement);

			logger.debug("Found information for file with ID: " + f.getID()
					+ ", Title: " + f.getTitle());
		} catch (Exception e) {
			logger.error("Failed midway through creating file object from XML",
					e);
		}

		// return created file instance
		return f;
	}
}
