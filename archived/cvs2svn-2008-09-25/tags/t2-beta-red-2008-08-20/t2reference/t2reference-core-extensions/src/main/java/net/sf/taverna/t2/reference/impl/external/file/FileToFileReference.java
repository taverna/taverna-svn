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
