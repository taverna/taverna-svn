/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.io.TransmitterException;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * @author mfortner
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
	    this.term = (String)((DataThing)inputMap.get("term")).getDataObject();
	    String maxRecs = (String)((DataThing)inputMap.get("maxRecords")).getDataObject();
	    this.maxRecords = Integer.parseInt(maxRecs);
	    
	   
	    	    
        
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
