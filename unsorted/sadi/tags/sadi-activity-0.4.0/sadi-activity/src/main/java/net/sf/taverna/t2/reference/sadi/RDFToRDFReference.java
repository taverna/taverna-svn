/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.reference.sadi;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Converts an RDFNode to a StringReference. 
 *
 * @author David Withers
 */
public class RDFToRDFReference implements ValueToReferenceConverterSPI {

	/**
	 * Can convert if the object is an instance of com.hp.hpl.jena.rdf.model.RDFNode
	 */
	public boolean canConvert(Object object, ReferenceContext arg1) {
		return object instanceof RDFNode;
	}

	public ExternalReferenceSPI convert(Object object, ReferenceContext arg1)
			throws ValueToReferenceConversionException {
		RDFReference result = new RDFReference();
		result.setValue((RDFNode) object);
		return result;
	}

}
