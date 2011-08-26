package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class is used to fetch pubmed articles in XML form. Use this worker only
 * if you already know the pubmed id.
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput id A comma-delimited list of PubMed IDs.
 * @tavinput rettype Indicates how complete the returned records should be:
 *           uilist, abstract, citation, medline, full (journals only)
 * @tavinput retmode Indicates the document format in which the results should
 *           be returned. Select one of the following values: "xml", "html",
 *           "text", "asn.1"
 * 
 * @tavoutput outputText An XML representation of the PubMed article
 */
public class PubMedEFetchWorker extends AbstractEFetchWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public PubMedEFetchWorker() {
		this.db = "pubmed";
		this.rettype = "full";
		this.retmode = "xml";
	}

	/**
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		this.id = inAdapter.getString("id");

		String tempRetMode = inAdapter.getString("retmode");
		this.retmode = (tempRetMode != null && !tempRetMode.equals("")) ? tempRetMode : retmode;

		String tempRetType = inAdapter.getString("rettype");
		this.rettype = (tempRetType != null && !tempRetType.equals("")) ? tempRetType : rettype;

		transmitterMap.put("id", this.id);
		transmitterMap.put("db", this.db);
		transmitterMap.put("rettype", this.rettype);
		transmitterMap.put("retmode", this.retmode);
		Map results;
		try {
			results = transmit(transmitterMap);
		} catch (MalformedURLException e) {
			throw new TaskExecutionException(e);
		} catch (TransmitterException e) {
			throw new TaskExecutionException(e);
		}
		return results;
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
