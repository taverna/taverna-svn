/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor fetches an Entrez Protein record from NCBI.
 * 
 * @author mfortner
 * @version $Revision: 1.2 $
 * 
 * @tavinput term The search term (usually a protein name or id).
 * @tavinput maxRecords The maximum number of records to be returned.
 * @tavinput outputFile A complete path to the output file.
 * @tavinput xslt A complete path to the XSLT used to transform the results.
 *           (optional)
 * 
 * @tavoutput resultsXml A string containing the resultant XML.
 * 
 */
public class EntrezProteinWorker extends AbstractNCBIWorker {

	public EntrezProteinWorker() {
		this.cmd = "Display";
		this.originalDb = "protein";
		this.startTag = "&lt;Seq-entry&gt;";

		this.endTag = "&lt;/Seq-entry&gt;";
		this.displayOption = "xml";
		this.url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
		this.rettype = "gp";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);

		this.term = inAdapter.getString("term");
		String maxRecs = inAdapter.getString("maxRecords");
		this.maxRecords = (maxRecs != null) ? Integer.parseInt(maxRecs) : 1;

		transmitterMap.put("db", "protein");
		transmitterMap.put("rettype", this.rettype);
		transmitterMap.put("term", this.term);
		transmitterMap.put("CMD", "Text");
		transmitterMap.put("cmd", "Search");
		transmitterMap.put("cmd_current", "search");
		transmitterMap.put("query_key", this.queryKey);
		transmitterMap.put("CrntRpt", "DocSum");
		transmitterMap.put("doptcmdl", "xml");
		transmitterMap.put("SUBMIT", "y");

		transmitterMap.put("dopt", this.displayOption);
		transmitterMap.put("orig_db", this.originalDb);
		transmitterMap.put("disp_max", String.valueOf(this.maxRecords));

		try {
			outputMap = this.transmit(transmitterMap);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new TaskExecutionException(e);
		} catch (TransmitterException e) {
			e.printStackTrace();
			throw new TaskExecutionException(e);
		} catch (Throwable th) {
			throw new TaskExecutionException(th);
		}

		return outputMap;
	}

}
