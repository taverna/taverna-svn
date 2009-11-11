/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.service.webservice.resource;

import java.net.URI;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.taverna.t2.service.model.Identifiable;

/**
 * A webservice resource.
 * 
 * @author David Withers
 */
@XmlRootElement(name = "Resource")
public class Resource {
	
	private Date created;
	
	private Date lastModified;

	private Long id;

	private URI uri;

	public static final String lineSeparator = System.getProperty("line.separator");

	public Resource() {
	}
	
	public Resource(Identifiable<Long> identifiable, URI uri) {
		this.id = identifiable.getId();
		this.created = identifiable.getCreated();
		this.lastModified = identifiable.getModified();
		this.uri = uri;
	}

	/**
	 * Returns the created time.
	 *
	 * @return the created time
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Sets the created time.
	 *
	 * @param created the new value for created time
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Returns the lastModified time.
	 *
	 * @return the value of lastModified time
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Sets the lastModified.
	 *
	 * @param lastModified the new value for lastModified
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Returns the id.
	 *
	 * @return the value of id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new value for id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the uri.
	 *
	 * @return the value of uri
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri the new value for uri
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID : ");
		sb.append(getId());
		sb.append(lineSeparator);
		sb.append("Created : ");
		sb.append(getCreated());
		sb.append(lineSeparator);
		sb.append("Modified : ");
		sb.append(getLastModified());
		sb.append(lineSeparator);
		sb.append("URI : ");
		sb.append(getUri());
		sb.append(lineSeparator);
		return sb.toString();
	}
}
