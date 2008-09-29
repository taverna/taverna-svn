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

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Abstract bean for serialising references, such as from
 * {@link DataDocumentBean}.
 * 
 * @see Beanable
 * @see DataDocumentBean
 * @see net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceBean
 * @see net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceBean
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ReferenceBean {
	//needs a default value or else it breaks, even if you tell it to auto-generate
	
	@SuppressWarnings("unused")
	@Id  //@GeneratedValue (strategy=GenerationType.AUTO)
	private String identifier = UUID.randomUUID().toString();;

//	public Long getIdentifier() {
//		return identifier;
//	}
//
//	public void setIdentitifer(Long identifier) {
//		this.identifier = identifier;
//	}

	/**
	 * Get the {@link Beanable} class that "owns" this bean. An instance of this
	 * class created with the default constructor should be able to use this
	 * {@link ReferenceBean} as a parameter to
	 * {@link Beanable#setFromBean(Object)}.
	 * 
	 * @return The owning class
	 */
	public abstract Class<? extends ReferenceScheme<? extends ReferenceBean>> getOwnerClass();
}
