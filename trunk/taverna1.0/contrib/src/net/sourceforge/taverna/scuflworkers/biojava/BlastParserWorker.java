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
 * @version $Revision: 1.1 $
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
            BlastLikeToXMLConverter  oBlast2XML = 
        		new BlastLikeToXMLConverter(fileUrl);

        	    if (tStrict) {
        	        oBlast2XML.setModeStrict();
        	    } else {
        	        oBlast2XML.setModeLazy();
        	    }
        	    oBlast2XML.convert();
        	    
        	    outAdapter.putString("blastresults",oBlast2XML.toString());
            
            /*
            //get the Blast input as a Stream
            InputStream is = new FileInputStream(fileUrl);

            //make a BlastLikeSAXParser
            BlastLikeSAXParser parser = new BlastLikeSAXParser();

            //make the SAX event adapter that will pass events to a Handler.
            SeqSimilarityAdapter adapter = new SeqSimilarityAdapter();

            //set the parsers SAX event adapter
            parser.setContentHandler(adapter);

            //The list to hold the SeqSimilaritySearchResults
            List results = new ArrayList();

            //create the SearchContentHandler that will build SeqSimilaritySearchResults
            //in the results List
            SearchContentHandler builder = new BlastLikeSearchBuilder(results,
                new DummySequenceDB("queries"), new DummySequenceDBInstallation());

            //register builder with adapter
            adapter.setSearchContentHandler(builder);

            //parse the file, after this the result List will be populated with
            //SeqSimilaritySearchResults
            parser.parse(new InputSource(is));
            
            

            //output some blast details
            for (Iterator i = results.iterator(); i.hasNext(); ) {
              SeqSimilaritySearchResult result =
                  (SeqSimilaritySearchResult)i.next();

              Annotation anno = result.getAnnotation();

              for (Iterator j = anno.keys().iterator(); j.hasNext(); ) {
                Object key = j.next();
                Object property = anno.getProperty(key);
                System.out.println(key+" : "+property);
              }
              System.out.println("Hits: ");

              //list the hits
              for (Iterator k = result.getHits().iterator(); k.hasNext(); ) {
                SeqSimilaritySearchHit hit =
                    (SeqSimilaritySearchHit)k.next();
                System.out.print("\tmatch: "+hit.getSubjectID());
                System.out.println("\te score: "+hit.getEValue());
              }

              System.out.println("\n");*/
            
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
