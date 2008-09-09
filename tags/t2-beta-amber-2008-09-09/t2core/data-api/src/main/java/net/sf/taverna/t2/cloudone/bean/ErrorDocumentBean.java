/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.cloudone.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Bean for serialising {@link ErrorDocument}. An ErrorDocument is serialised
 * as a String identifier from {@link #getIdentifier()}, an optional message in
 * {@link #getMessage()} and optional stack trace in {@link #getStackTrace()}.
 * 
 * @see Beanable
 * @see ErrorDocument
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "errorDocument")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "errorDocument")
public class ErrorDocumentBean {
	@Id
	private String identifier;
	private String message;
	@Column(length=32672)  //this is the largest derby can do
	private String stackTrace;

	public String getIdentifier() {
		return identifier;
	}

	public String getMessage() {
		return message;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
