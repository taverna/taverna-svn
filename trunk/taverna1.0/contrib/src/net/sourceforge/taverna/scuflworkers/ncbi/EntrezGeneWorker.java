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
 * This class is responsible for fetching an Entrez Gene record in XML format.
 * @author mfortner
 * @version $Revision: 1.3 $
 */
public class EntrezGeneWorker extends AbstractNCBIWorker {
	
	public EntrezGeneWorker(){
		//this.url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
		this.url="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";
		this.startTag = "<Entrezgene>";
		this.endTag = "</Entrezgene>";
		this.originalDb = "gene";		
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
	    DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
	    
	    this.term = inAdapter.getString("term");
	    String maxRecs = inAdapter.getString("maxRecords");
	    
	    this.maxRecords = (maxRecs == null)?1:Integer.parseInt(maxRecs);
	    	    
        
        transmitterMap.put("db",this.originalDb);       
        transmitterMap.put("retmode","xml");
        transmitterMap.put("id",this.term);
        //transmitterMap.put("CMD","Text");
        //transmitterMap.put("cmd","Search");
        //transmitterMap.put("cmd_current","search");
        //transmitterMap.put("query_key",this.queryKey);
        //transmitterMap.put("CrntRpt","DocSum");
        //transmitterMap.put("doptcmdl","xml");
        //transmitterMap.put("SUBMIT","y");
        
        //transmitterMap.put("dopt",this.displayOption);
        //transmitterMap.put("orig_db",this.originalDb);
        //transmitterMap.put("disp_max",String.valueOf(this.maxRecords));		
		
        try {
            outputMap = this.transmit(transmitterMap);
        } catch (MalformedURLException e) {
           throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            e.printStackTrace();
            throw new TaskExecutionException(e);
        }catch (Throwable th){
            th.printStackTrace();
        }
		System.gc();
				
		return outputMap;
	}


}
