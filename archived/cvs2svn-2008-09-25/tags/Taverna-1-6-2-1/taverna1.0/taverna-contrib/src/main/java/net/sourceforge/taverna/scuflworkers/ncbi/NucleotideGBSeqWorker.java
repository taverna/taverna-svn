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
 * This processor returns a GB Seq formatted record.
 * 
 * @author mfortner
 * @version $Revision: 1.2 $
 * 
 * @tavinput id The nucleotide accession.
 * @tavoutput outputText a GB Seq formatted record
 */
public class NucleotideGBSeqWorker extends AbstractEFetchWorker {
	
	private static Logger logger = Logger.getLogger(NucleotideGBSeqWorker.class);

	public NucleotideGBSeqWorker() {
		this.db = "nucleotide";
		this.retmode = "xml";
		this.rettype = "gb";
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
		transmitterMap.put("db", this.db);
		transmitterMap.put("rettype", this.rettype);
		transmitterMap.put("retmode", this.retmode);

		try {
			outputMap = this.transmit(transmitterMap);
		} catch (MalformedURLException e) {
			logger.error(this.url);
			throw new TaskExecutionException(e);
		} catch (TransmitterException e) {
			throw new TaskExecutionException(e);
		}

		return outputMap;
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
