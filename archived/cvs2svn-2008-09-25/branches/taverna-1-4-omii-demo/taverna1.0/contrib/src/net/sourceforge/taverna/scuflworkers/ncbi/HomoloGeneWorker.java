/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class fetches HomoloGene data from NCBI.
 * @author mfortner
 *
 */
public class HomoloGeneWorker extends AbstractNCBIWorker {
	
	public HomoloGeneWorker(){
		this.originalDb = "homologene";
		this.startTag = "&lt;HomoloGeneEntry&gt;";
		this.endTag = "&lt;/HomoloGeneEntry&gt;";
		
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
	    DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
	    this.term = inAdapter.getString("term");
	    String maxRecs = inAdapter.getString("maxRecords");
	    this.maxRecords = (maxRecs != null)?Integer.parseInt(maxRecs): 10;
	    
	   
	    	    
        
        transmitterMap.put("db","homologene");       
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
            System.out.println(this.url);
           throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            throw new TaskExecutionException(e);
        }
		
		
		
		
		return outputMap;
	}

}
