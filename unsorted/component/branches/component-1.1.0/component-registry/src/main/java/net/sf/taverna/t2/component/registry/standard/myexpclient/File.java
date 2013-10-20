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
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Access.access;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.FILE;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveAttributions;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveCredits;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveTags;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class File extends Resource {
	private static final long serialVersionUID = -9051574197214290716L;

	private Access accessType;
	private User uploader;
	private License license;
	private String filename;
	private String visibleType;
	private String contentType;
	private final List<Tag> tags = new ArrayList<Tag>();
	private final List<Resource> credits = new ArrayList<Resource>();
	private final List<Resource> attributions = new ArrayList<Resource>();

	public File() {
		super(FILE);
	}

	File(Element root, Logger logger) {
		super(FILE, root, logger);
	}

	@Override
	public Access getAccessType() {
		return accessType;
	}

	public void setAccessType(Access accessType) {
		this.accessType = accessType;
	}

	public List<Tag> getTags() {
		return tags;
	}

	@Override
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
		return filename;
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

	@Override
	public String getVisibleType() {
		return visibleType;
	}

	public void setVisibleType(String visibleType) {
		this.visibleType = visibleType;
	}

	public List<Resource> getCredits() {
		return credits;
	}

	public List<Resource> getAttributions() {
		return attributions;
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
	@SuppressWarnings("incomplete-switch")
	public static String getRequiredAPIElements(RequestType type) {
		String elements = "";

		/*
		 * cases higher up in the list are supersets of those that come below -
		 * hence no "break" statements are required, because 'falling through'
		 * the switch statement is the desired behaviour in this case
		 */
		switch (type) {
		case PREVIEW:
			elements += "filename,content-type,created-at,updated-at,"
					+ "license-type,tags,credits,attributions,";
		case FULL_LISTING:
			elements += "uploader,type,";
		case SHORT_LISTING:
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

	/** build a file instance from XML */
	public static File buildFromXML(Element root, Logger logger) {
		/*
		 * return null to indicate an error if XML document contains no root
		 * element
		 */
		if (root == null)
			return null;

		File f = new File();

		try {
			f = new File(root, logger);
			f.setFilename(getChildText(root, "filename"));
			f.setUploader(makeUser(getChild(root, "uploader")));
			f.setAccessType(access(getChild(root, "privileges")));
			f.setLicense(License
					.getInstance(getChildText(root, "license-type")));
			f.setVisibleType(getChildText(root, "type"));
			f.setContentType(getChildText(root, "content-type"));
			f.getTags().addAll(retrieveTags(root));
			f.getCredits().addAll(retrieveCredits(root));
			f.getAttributions().addAll(retrieveAttributions(root));

			logger.debug(format(
					"Found information for file with ID: %s, Title: %s",
					f.getID(), f.getTitle()));
		} catch (Exception e) {
			logger.error("Failed midway through creating file object from XML",
					e);
		}

		// return created file instance
		return f;
	}
}
