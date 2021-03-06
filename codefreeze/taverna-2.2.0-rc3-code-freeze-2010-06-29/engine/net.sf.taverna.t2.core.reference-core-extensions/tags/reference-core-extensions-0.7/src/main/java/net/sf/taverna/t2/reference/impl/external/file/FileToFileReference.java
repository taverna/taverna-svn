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
package net.sf.taverna.t2.reference.impl.external.file;

import java.io.File;
import java.io.IOException;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 * Converts java.lang.File instances to FileReference reference type
 * 
 * @author Tom Oinn
 * 
 */
public class FileToFileReference implements ValueToReferenceConverterSPI {

	/**
	 * TODO - should probably do more sophisticated checks such as whether the
	 * file is a file or directory etc etc, for now just checks whether the
	 * specified object is a java.io.File
	 */
	public boolean canConvert(Object o, ReferenceContext context) {
		return (o instanceof File);
	}

	/**
	 * Return a FileReference
	 */
	public ExternalReferenceSPI convert(Object o, ReferenceContext context)
			throws ValueToReferenceConversionException {
		FileReference result = new FileReference();
		try {
			result.setFilePath(((File) o).getCanonicalPath());
		} catch (IOException ioe) {
			throw new ValueToReferenceConversionException(ioe);
		}
		return result;
	}

}
