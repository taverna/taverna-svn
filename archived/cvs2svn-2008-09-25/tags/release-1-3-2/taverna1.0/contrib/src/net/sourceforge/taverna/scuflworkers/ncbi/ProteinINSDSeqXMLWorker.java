package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor fetches an INSD formatted protein record
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput id	The protein accession.
 * @tavoutput outputText an INSD formatted protein record
 */
public class ProteinINSDSeqXMLWorker extends AbstractEFetchWorker {
    
	/**
	 * Constructor
	 *
	 */
    public ProteinINSDSeqXMLWorker(){
	    this.db="protein";
	    this.retmode="xml";
	    this.rettype="gpc";
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);        
	    this.id = inAdapter.getString("id");

	    	    
        transmitterMap.put("id",this.id);
        transmitterMap.put("db",this.db);       
        transmitterMap.put("rettype",this.rettype);
        transmitterMap.put("retmode",this.retmode);	
		
        try {
            outputMap = this.transmit(transmitterMap);
        } catch (MalformedURLException e) {
            System.out.println(this.url);
           throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            throw new TaskExecutionException(e);
        } catch (Throwable th){
            throw new TaskExecutionException(th);
        }
		
		return outputMap;
    }
    
    /**
     * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractEFetchWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"id"};
    }

    /**
     * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractEFetchWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'"};
    }
    
}
