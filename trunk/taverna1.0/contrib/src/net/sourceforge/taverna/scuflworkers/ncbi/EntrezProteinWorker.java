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
 * 
 * @author mfortner
 * @version $Revision: 1.1 $
 */
public class EntrezProteinWorker extends AbstractNCBIWorker {
	
	public EntrezProteinWorker(){
		this.cmd = "Display";
		this.originalDb = "gene";
		this.startTag = "<Bioseq-set>";
		this.endTag = "</Bioseq-set>";
		this.displayOption = "gene_protein";
		this.url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
	    this.term = (String)((DataThing)inputMap.get("term")).getDataObject();
	    String maxRecs = (String)((DataThing)inputMap.get("maxRecords")).getDataObject();
	    this.maxRecords = (maxRecs != null)?Integer.parseInt(maxRecs): -1;
	    	    
        
        transmitterMap.put("DB","protein");       
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
            e.printStackTrace();
           throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            e.printStackTrace();
            throw new TaskExecutionException(e);
        } catch (Throwable th){
            throw new TaskExecutionException(th);
        }
		
				
		return outputMap;
	}

}
