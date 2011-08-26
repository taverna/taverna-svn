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
 * This class fetches Nucleotide XML documents.
 * @author mfortner
 */
public class NucleotideXMLWorker extends AbstractNCBIWorker{
	
	public NucleotideXMLWorker(){
		this.startTag = "&lt;Seq-entry&gt;";
		this.endTag = "&lt;/Seq-entry&gt;";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
	    DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
	    
	    this.term = inAdapter.getString("term");
	    String maxRecs = inAdapter.getString("maxRecords");
	    
	    this.maxRecords = (maxRecs == null)?1:Integer.parseInt(maxRecs);
	    	    
        
        transmitterMap.put("db","Nucleotide");       
        transmitterMap.put("rettype",this.rettype);
        transmitterMap.put("term",this.term);
        transmitterMap.put("CMD","search");
        transmitterMap.put("cmd","Search");
        transmitterMap.put("cmd_current","search");
        transmitterMap.put("query_key",this.queryKey);
        transmitterMap.put("CrntRpt","DocSum");
        transmitterMap.put("doptcmdl","xml");
        transmitterMap.put("SUBMIT","y");
        
        transmitterMap.put("dopt",this.displayOption);
        transmitterMap.put("orig_db",this.originalDb);
        transmitterMap.put("disp_max",String.valueOf(this.maxRecords));		
		
        try {
            outputMap = this.transmit(transmitterMap);
        } catch (MalformedURLException e) {
           throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            throw new TaskExecutionException(e);
        }
		
		
		
		
		return outputMap;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#inputNames()
	 */

}
