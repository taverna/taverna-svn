package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class searches for articles in PubMed.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class PubMedESearchWorker extends AbstractEFetchWorker {
    
    protected String term = null;
    protected String field = null;
    private String mindate = null;
    private String maxdate = null;
    
    public PubMedESearchWorker(){
        this.url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
        this.db = "pubmed";
        this.rettype = null;//"uilist";
        this.retmode = "xml";
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        this.term =  inAdapter.getString("term");
        this.field = inAdapter.getString("field");
        this.retstart = inAdapter.getString("retstart");
        this.retmax = inAdapter.getString("retmax");
        this.mindate = inAdapter.getString("mindate");
        this.maxdate = inAdapter.getString("maxdate");
        
        transmitterMap.put("term", this.term);
        
        if (this.field != null){
            transmitterMap.put("field",this.field);
        }
        
        if (this.retstart != null){
            transmitterMap.put("retstart", this.retstart);
        }
        if (this.retmax != null){
            transmitterMap.put("retmax", this.retmax);
        }
        
        if (this.mindate != null){
            transmitterMap.put("mindate",this.mindate);
        }
        
        if (this.maxdate != null){
            transmitterMap.put("maxdate",this.maxdate);
        }
        transmitterMap.put("db", this.db); 
        
        if (this.rettype != null){
            transmitterMap.put("rettype", this.rettype);
        }
        
        
        transmitterMap.put("retmode", this.retmode);
        
        transmitterMap.put("tool","taverna");
        Map results;
        try {
            results = transmit(transmitterMap);
        } catch (MalformedURLException e) {
            throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            throw new TaskExecutionException(e);
        }catch (Throwable th){
            throw new TaskExecutionException(th);
        }
        return results;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
       return new String[]{"term","db","field","retstart","retmax","mindate","maxdate","rettype"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'"};
    }

}
