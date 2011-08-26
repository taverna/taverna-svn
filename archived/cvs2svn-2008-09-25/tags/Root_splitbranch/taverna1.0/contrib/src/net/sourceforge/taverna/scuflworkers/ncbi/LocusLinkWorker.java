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
 * This class is designed to fetch locuslink data from the NCBI
 * database as XML.
 * @author mfortner
 */
public class LocusLinkWorker extends AbstractEFetchWorker {
	
	public LocusLinkWorker(){
        this.db = "locuslink";
        this.rettype = "full";
        this.retmode = "xml";
	}

	/* (non-Javadoc)
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
        return new String[] { "id", "rettype", "retmode" };
    }

    /**
     * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractEFetchWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[] { "'text/plain'","'text/plain'","'text/plain'" };
    }
}
