package net.sourceforge.taverna.scuflworkers.biojava;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.BlastLikeToXMLConverter;
import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.biojava.bio.program.ssbind.BlastLikeSearchBuilder;
import org.biojava.bio.program.ssbind.SeqSimilarityAdapter;
import org.biojava.bio.search.SearchContentHandler;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.seq.db.DummySequenceDB;
import org.biojava.bio.seq.db.DummySequenceDBInstallation;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.xml.sax.InputSource;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class parses blast results and returns an XML document containing the results.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class BlastParserWorker implements LocalWorker {
    
    public BlastParserWorker(){
        
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        String fileUrl = inAdapter.getString("fileUrl");
        
        String strictStr = inAdapter.getString("strict");
        boolean tStrict = (strictStr != null)?Boolean.getBoolean(strictStr):false;
        
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        try {
            
            
            BlastLikeToXMLConverter  oBlast2XML = new BlastLikeToXMLConverter(fileUrl);
            
        	    if (tStrict) {
        	        oBlast2XML.setModeStrict();
        	    } else {
        	        oBlast2XML.setModeLazy();
        	    }
        	    oBlast2XML.convert();
        	    
        	    outAdapter.putString("blastresults",oBlast2XML.toString());
            
            
            }catch (Exception ex){
                throw new TaskExecutionException(ex);
            }
           
        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"fileUrl","strict"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[]{"blastresults"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[]{"'text/xml'"};
    }

}
