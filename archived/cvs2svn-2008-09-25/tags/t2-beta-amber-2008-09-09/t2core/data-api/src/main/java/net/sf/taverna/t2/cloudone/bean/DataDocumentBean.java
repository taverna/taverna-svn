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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Bean for serialising a {@link DataDocument}. A DataDocument is serialised as
 * a String identifier from {@link #getIdentifier()}, and a list of
 * {@link ReferenceBean}s (serialised {@link ReferenceScheme}s) from
 * {@link #getReferences()}
 * 
 * @see Beanable
 * @see DataDocument
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "dataDocument")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "dataDocument")
public class DataDocumentBean {
	@Id
	private String identifier;
	@ManyToMany //(cascade=CascadeType.ALL)
	private List<ReferenceBean> references = new ArrayList<ReferenceBean>();

	public String getIdentifier() {
		return identifier;
	}

	@XmlElement(name = "reference")
//	@Column(name="reference")
	//@OneToMany
	public List<ReferenceBean> getReferences() {
		return references;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setReferences(List<ReferenceBean> references) {
		this.references = references;
	}

}
