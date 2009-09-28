/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice.ext.pc3;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.provenance.lineageservice.utils.DataValueExtractor;
import net.sf.taverna.t2.provenance.opm.OPMManager;

/**
 * @author paolo
 * spec. of OPMManager that understands data types used in the PAN-STARRS workflow (CSVFileEntry and DatabaseEntry)
 *
 */
public class PANSTARRSOPMManager extends OPMManager {


	List<DataValueExtractor> extractors = new ArrayList<DataValueExtractor>();


	/**
	 * provides instances of the DataValueExtractors it knows about
	 */
	public PANSTARRSOPMManager() {
		super();
		extractors.add(new CSVFileEntryValueExtractor());
		extractors.add(new DatabaseEntryValueExtractor());
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.utils.OPMManager#getDataValueExtractor()
	 */
	@Override
	public List<DataValueExtractor> getDataValueExtractor() {
		return extractors;
	}

}
