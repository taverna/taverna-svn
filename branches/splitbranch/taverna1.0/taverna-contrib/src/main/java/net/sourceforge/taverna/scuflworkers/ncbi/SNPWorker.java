/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor fetches SNP records.
 * 
 * @author mfortner
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput id A comma-delimited list of SNP IDs
 * @tavinput rettype
 * @tavinput retmode
 * 
 * @tavoutput resultsXml An XML representation of SNP records.
 */
public class SNPWorker extends AbstractNCBIWorker {
	
	private static Logger logger = Logger.getLogger(SNPWorker.class);

	private String id;

	private String db;

	private String retmode;

	/**
	 * Default constructor
	 * 
	 */
	public SNPWorker() {
		this.startTag = "&lt;NSE-rs&gt;";
		this.endTag = "&lt;/NSE-rs&gt;";
		this.originalDb = "snp";
		this.queryKey = "13";
		this.db = "snp";
		this.rettype = "xml";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		this.id = inAdapter.getString("id");

		transmitterMap.put("id", this.id);
		transmitterMap.put("DB", this.db);
		transmitterMap.put("rettype", this.rettype);
		transmitterMap.put("retmode", this.retmode);
		transmitterMap.put("CMD", "Display");

		try {
			outputMap = this.transmit(transmitterMap);
		} catch (MalformedURLException e) {
			logger.error(this.url);
			throw new TaskExecutionException(e);
		} catch (TransmitterException e) {
			throw new TaskExecutionException(e);
		} catch (Throwable th) {
			throw new TaskExecutionException(th);
		}

		return outputMap;
	}

	/**
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractEFetchWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "id", "rettype", "retmode" };
	}

	/**
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractEFetchWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'" };
	}

}
