package net.sourceforge.taverna.scuflworkers.biojava;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class SwissProtParserWorker implements LocalWorker {

    public SwissProtParserWorker() {

    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        BufferedReader br = null;
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        String fileUrl = inAdapter.getString("fileUrl");

        
        HashMap outputMap = new HashMap();
        try {

            //create a buffered reader to read the sequence file specified by
            // args[0]
            br = new BufferedReader(new FileReader(fileUrl));

            //read the EMBL File
            SequenceIterator sequences = SeqIOTools.readEmbl(br);

            //iterate through the sequences

            while (sequences.hasNext()) {

                Sequence seq = sequences.nextSequence();
                //do stuff with the sequence
            }

        } catch (FileNotFoundException ex) {
            throw new TaskExecutionException(ex);
        } catch (BioException ex) {
            throw new TaskExecutionException(ex);
        } catch (NoSuchElementException ex) {
            throw new TaskExecutionException(ex);
        }

        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"fileUrl"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain"};
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
        return new String[]{"'text/plain'"};
    }


}