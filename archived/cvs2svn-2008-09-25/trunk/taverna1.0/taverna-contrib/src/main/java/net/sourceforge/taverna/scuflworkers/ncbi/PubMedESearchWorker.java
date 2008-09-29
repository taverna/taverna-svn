package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor searches for articles in PubMed and returns their IDs in XML
 * format.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput term The search term.
 * @tavinput db The database to be searched (default=pubmed).
 * @tavinput field The optional database field (affl, auth, ecno, jour, iss,
 *           mesh, majr, mhda, page, pdat, ptyp, si, subs, subh, tiab, word,
 *           titl, lang, uid, fltr, vol)
 * @tavinput retstart (x= sequential number of the first record retrieved -
 *           default=0 which will retrieve the first record)
 * @tavinput retmax (y= number of items retrieved)
 * @tavinput mindate The minimum date from which to begin the search.
 * @tavinput maxdate The maximu date from which to end the search.
 * @tavinput rettype count, uilist (default)
 * 
 * @tavoutput outputText a PubMed ID list in XML format.
 * 
 */
public class PubMedESearchWorker extends AbstractEFetchWorker {

	protected String term = null;

	protected String field = null;

	private String mindate = null;

	private String maxdate = null;

	public PubMedESearchWorker() {
		this.url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
		this.db = "pubmed";
		this.rettype = null;// "uilist";
		this.retmode = "xml";
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		this.term = inAdapter.getString("term");
		this.field = inAdapter.getString("field");
		this.retstart = inAdapter.getString("retstart");
		this.retmax = inAdapter.getString("retmax");
		this.mindate = inAdapter.getString("mindate");
		this.maxdate = inAdapter.getString("maxdate");

		transmitterMap.put("term", this.term);

		if (this.field != null) {
			transmitterMap.put("field", this.field);
		}

		if (this.retstart != null) {
			transmitterMap.put("retstart", this.retstart);
		}
		if (this.retmax != null) {
			transmitterMap.put("retmax", this.retmax);
		}

		if (this.mindate != null) {
			transmitterMap.put("mindate", this.mindate);
		}

		if (this.maxdate != null) {
			transmitterMap.put("maxdate", this.maxdate);
		}
		transmitterMap.put("db", this.db);

		if (this.rettype != null) {
			transmitterMap.put("rettype", this.rettype);
		}

		transmitterMap.put("retmode", this.retmode);

		transmitterMap.put("tool", "taverna");
		Map results;
		try {
			results = transmit(transmitterMap);
		} catch (MalformedURLException e) {
			throw new TaskExecutionException(e);
		} catch (TransmitterException e) {
			throw new TaskExecutionException(e);
		} catch (Throwable th) {
			throw new TaskExecutionException(th);
		}
		return results;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "term", "db", "field", "retstart", "retmax", "mindate", "maxdate", "rettype" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'",
				"'text/plain'", "'text/plain'", "'text/plain'" };
	}

}
