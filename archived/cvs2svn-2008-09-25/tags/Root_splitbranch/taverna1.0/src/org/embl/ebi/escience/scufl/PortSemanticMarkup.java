/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: PortSemanticMarkup.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-06-21 12:36:44 $
 *               by   $Author: davidwithers $
 * Created on 21-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.scufl;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * A SemanticMarkup for Ports that fires ScuflModelEvents when markup elements
 * are changed.
 * 
 * @author David Withers
 */
public class PortSemanticMarkup extends SemanticMarkup {

	/**
     * Create a new item of semantic markup for the Port specified. This
     * should be interpreted as 'this markup object applies to the supplied
     * Port'.
     */
	public PortSemanticMarkup(Object subject) {
		super(subject);
	}

	public PortSemanticMarkup(PortSemanticMarkup other) {
		super(other);
	}
	
	/**
	 * Set the free text description
	 */
	public void setDescription(String theDescription) {
		super.setDescription(theDescription);
		fireModelEvent();
	}

	/**
	 * Clear the array of MIME types
	 */
	public void clearMIMETypes() {
		synchronized (this.mimeTypeList) {
			this.mimeTypeList.clear();
			fireModelEvent();
		}
	}

	/**
	 * Add a MIME type
	 */
	public void addMIMEType(String mimeType) {
		synchronized (this.mimeTypeList) {
			if (mimeType != null && mimeType.equals("") == false) {
				// fixme:
				// mimeTypeList.contains(mimeType) may be more efficient [mrp]
				String[] types = mimeType.split(",");
				for (int j = 0; j < types.length; j++) {
					boolean foundType = false;
					for (Iterator i = this.mimeTypeList.iterator(); i.hasNext();) {
						// fixme:
						// is it intended that we double-check each element is a
						// string?
						// is this not checked out earlier? [mrp]
						if (((String) i.next()).equals(types[j])) {
							// Bail if we already have one
							foundType = true;
						}
					}
					if (!foundType) {
						this.mimeTypeList.add(types[j]);
						fireModelEvent();
					}
				}
			}
		}
	}

	public void setMIMETypes(List mimeTypes) {
		synchronized (this.mimeTypeList) {
			this.mimeTypeList.clear();
			for (Iterator i = mimeTypes.iterator(); i.hasNext();) {
				String mt = (String) i.next();
				if (!this.mimeTypeList.contains(mt)) {
					this.mimeTypeList.add(mt);
				}
			}

			fireModelEvent();
		}
	}

	/**
	 * Set the semantic markup as a string, not the best way to do things but
	 * will have to do for now
	 */
	public void setSemanticType(String newSemanticType) {
		super.setSemanticType(newSemanticType);
		fireModelEvent();
	}

	/**
	 * Configure this markup object from the supplied XML element. This is
	 * assuming that the element passed in is the 'metadata' element in the
	 * XScufl namespace.
	 */
	public void configureFromElement(Element theElement) {
		super.configureFromElement(theElement);
		fireModelEvent();
	}
	
	/**
	 * If the subject of this metadata is a port then fire events off when
	 * things are changed.
	 */
	void fireModelEvent() {
		if (this.subject instanceof Port) {
			((Port) this.subject).fireModelEvent(new ScuflModelEvent(this,
					"Metadata change"));
		}
	}

}
