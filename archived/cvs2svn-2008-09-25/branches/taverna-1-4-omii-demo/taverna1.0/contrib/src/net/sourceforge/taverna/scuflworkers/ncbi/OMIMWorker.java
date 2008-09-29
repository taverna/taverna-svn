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
 * This processor fetches an OMIM record from the NCBI database in XML format.
 * @author mfortner
 * @version $Revision: 1.2 $
 * 
 * @tavinput term  The search term.
 * @tavoutput outputText an XML formatted OMIM record.
 */
public class OMIMWorker extends AbstractNCBIWorker {
	
	/**
	 * Constructor
	 *
	 */
	public OMIMWorker(){
		this.url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
		this.startTag = "&lt;Mim-entry&gt;";
		this.endTag = "&lt;/Mim-entry&gt;";
		this.originalDb = "omim";		
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
	    DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
	    this.term = inAdapter.getString("term");
	    String maxRecs = inAdapter.getString("maxRecords");
	    if (maxRecs != null){
	        this.maxRecords = Integer.parseInt(maxRecs);
	    }	    
        
        transmitterMap.put("db",this.originalDb);       
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
        } catch (Throwable th){
            throw new TaskExecutionException(th);
        }
		
				
		return outputMap;
	}
 

}
