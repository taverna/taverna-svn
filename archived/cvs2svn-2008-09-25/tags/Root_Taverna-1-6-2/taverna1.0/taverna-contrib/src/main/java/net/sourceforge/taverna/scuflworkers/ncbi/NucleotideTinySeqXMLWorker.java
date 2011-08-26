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
 * This processor fetches a nucleotide sequence from NCBI and returns the
 * results in the TinySeqXML format.
 * 
 * Last edited by: $Author: sowen70 $
 * 
 * @author mfortner
 * @version $Revision: 1.3 $
 * 
 * @tavinput id The nucleotide accession.
 * @tavoutput outputText a TinySeqXML formatted nucleotide record
 */
public class NucleotideTinySeqXMLWorker extends AbstractEFetchWorker {
	// http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&id=5&rettype=fasta&retmode=xml

	/**
	 * Constructor
	 */
	public NucleotideTinySeqXMLWorker() {
		this.db = "nucleotide";
		this.rettype = "fasta";
		this.retmode = "xml";
	}

	/**
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		this.id = inAdapter.getString("id");

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
		return new String[] { "id" };
	}

	/**
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractEFetchWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
	}
}
