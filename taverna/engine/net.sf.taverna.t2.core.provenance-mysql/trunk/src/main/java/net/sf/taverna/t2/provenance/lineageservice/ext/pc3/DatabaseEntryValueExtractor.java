/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice.ext.pc3;

import info.ipaw.pc3.PSLoadWorkflow.DatabaseEntry;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.sf.taverna.t2.provenance.lineageservice.utils.DataValueExtractor;

/**
 * @author paolo
 * designed to support provenance analysis in the PAN-STARRS provenance challenge workflow
 * <br/> this method specifically extracts a filename from an input XMLEncoded bean that represents a PAN-STARRS CSVFileEntry
 *
 */
public class DatabaseEntryValueExtractor implements DataValueExtractor {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.utils.DataValueExtractor#extractString(java.lang.Object)
	 */
	
	/**
	 * extracts a filepath from an XMLEncoded bean that represents a PAN-STARRS CSVFileEntry
	 */
	public String extractString(Object complexContent) {

		String complexContentStr = (String) complexContent;
	    InputStream IStream = new ByteArrayInputStream(complexContentStr.getBytes()); 
	    XMLDecoder decoder = new XMLDecoder(IStream);
	    DatabaseEntry output = (DatabaseEntry) decoder.readObject();
	    decoder.close(); 
	    
		return output.getDBName();
	}

}
