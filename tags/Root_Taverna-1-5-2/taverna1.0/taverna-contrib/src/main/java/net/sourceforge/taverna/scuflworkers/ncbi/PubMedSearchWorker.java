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
 * This class downloads PubMed records in XML format. Since NCBI does not
 * currently support a pure XML
 * 
 * @author mfortner
 * @version $Revision: 1.2 $
 */
public class PubMedSearchWorker extends AbstractNCBIWorker {
	
	private static Logger logger = Logger.getLogger(PubMedSearchWorker.class);

	public PubMedSearchWorker() {
		this.startTag = "<font color=\"#4040FF\">&lt;</font><font color=\"#C04000\">PubmedArticle</font><font color=\"#4040FF\">&gt;</font>";
		this.endTag = "<font color=\"#4040FF\">&lt;/</font><font color=\"#C04000\">PubmedArticle</font><font color=\"#4040FF\">&gt;</font>";
		this.originalDb = "PubMed";
		this.url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
		this.cmd = "search";
		this.cmdCurrent = "search";
		this.queryKey = "1";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inputMapAdapter = new DataThingAdapter(inputMap);
		this.term = inputMapAdapter.getString("term");
		String maxRecs = inputMapAdapter.getString("disp_max");
		// String maxRecs = (String)inputMap.get("maxRecords");
		this.maxRecords = Integer.parseInt(maxRecs);

		transmitterMap.put("db", "PubMed");
		transmitterMap.put("rettype", this.rettype);
		transmitterMap.put("term", this.term);
		transmitterMap.put("CMD", "search");
		transmitterMap.put("cmd", "Search");
		transmitterMap.put("cmd_current", "search");
		transmitterMap.put("query_key", this.queryKey);
		transmitterMap.put("CrntRpt", "DocSum");
		transmitterMap.put("doptcmdl", "xml");
		transmitterMap.put("SUBMIT", "y");

		transmitterMap.put("dopt", this.displayOption);
		transmitterMap.put("orig_db", this.originalDb);
		transmitterMap.put("disp_max", maxRecs);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "term", "database", "minDate", "maxDate", "reldate", "rettype", "cmd", "cmd_current",
				"dopt", "orig_db", "disp_max" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'",
				"'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'",
				"'text/plain'", "'text/plain'" };
	}

}
