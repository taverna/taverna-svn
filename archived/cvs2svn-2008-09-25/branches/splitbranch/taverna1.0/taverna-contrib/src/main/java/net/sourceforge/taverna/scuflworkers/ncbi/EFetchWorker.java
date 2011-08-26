package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class provides a wrapper for the NCBI EFetch Utilities.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class EFetchWorker extends AbstractEFetchWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);

		HashMap outputMap = new HashMap();

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 * @link http://eutils.ncbi.nlm.nih.gov/entrez/query/static/efetchseq_help.html
	 */
	public String[] inputNames() {
		return new String[] { "id", "retstart", "retmax", "strand", "seq_start", "seq_stop", "complexity", "retmode",
				"rettype", };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'",
				"'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "outputText" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}
