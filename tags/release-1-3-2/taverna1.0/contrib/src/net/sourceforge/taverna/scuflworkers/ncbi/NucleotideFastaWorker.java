package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor fetches a nucleotide sequence in FASTA format.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput id	The nucleotide accession.
 * @tavoutput outputText a FASTA-formatted sequence
 */
public class NucleotideFastaWorker extends AbstractEFetchWorker{
    
    public NucleotideFastaWorker(){
        this.db = "nucleotide";
	    this.retmode="text";
	    this.rettype="fasta";        
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
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
