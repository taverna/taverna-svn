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
 * This processor is responsible for fetching an Entrez Gene record in XML format.
 * It can also transform the resulting XML document.
 * 
 * @author mfortner
 * @version $Revision: 1.6 $
 * 
 * @tavinput term			The search term (usually a gene name).
 * @tavinput maxRecords		The maximum number of records to be returned.
 * @tavinput outputFile		A complete path to the output file.
 * @tavinput xslt			A complete path to the XSLT used to transform the results. (optional)
 * 
 * @tavoutput resultsXml	A string containing the resultant XML.
 */
public class EntrezGeneWorker extends AbstractNCBIWorker {
	
	public EntrezGeneWorker(){
		//this.url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
		this.url="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";
		this.startTag = "<Entrezgene>";
		this.endTag = "</Entrezgene>";
		this.originalDb = "gene";		
	}

	/*
	 * 
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		this.inputMap = inputMap;
	    DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
	    
	    this.term = inAdapter.getString("term");
	    String maxRecs = inAdapter.getString("maxRecords");
	    
	    this.maxRecords = (maxRecs == null)?1:Integer.parseInt(maxRecs);
	    	    
        
        transmitterMap.put("db",this.originalDb);       
        transmitterMap.put("retmode","xml");
        transmitterMap.put("id",this.term);
		
        
		
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
