package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.GetStreamProcessor;
import net.sourceforge.taverna.io.GetStreamTransmitter;
import net.sourceforge.taverna.io.StreamProcessor;
import net.sourceforge.taverna.io.StreamTransmitter;
import net.sourceforge.taverna.io.TransmitterException;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class provides the basis for all NCBI EFetch Tasks.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public abstract class AbstractEFetchWorker implements LocalWorker {

    /**
     * The return format.  Values include (xml|html|text|asn.1)
     */
    protected String retmode;
    
    /**
     *  Defines the output types based on database
     *  native 	Default format for viewing sequences.
     *  fasta 	FASTA view of a sequence.
     *  gb 		GenBank view for sequences, constructed sequences will be shown as contigs (by pointing to its parts).  Valid for nucleotides.
     *  gbc 	INSDSeq structured flat file for Nucleotides.
     *  gbwithparts 	GenBank view for sequences, the sequence will always be shown. Valid for Nucleotides
     *  est 	EST Report. Valid for sequences from dbEST database.
     *  gss 	GSS Report. Valid for sequences from dbGSS database.
     *  gp 		GenPept view. Valid for Proteins.
     *  gpc 	INSDSeq structured flat file for Proteins.
     *  seqid 	To convert list of gis into list of seqids.
     *  acc 	To convert list of gis into list of accessions. 
     *  chr 	SNP Chromosome Report. 
     *  flt 	SNP Flat File report. 
     *  rsr 	SNP RS Cluster report. 
     *  brief 	SNP ID list. 
     *  docset 	SNP RS summary. 
     */
    protected String rettype;
    
    /**
     * sequential number of the first id retrieved - default=0 which will retrieve the first record)
     */
    protected String retstart = "0";
    
    /**
     * The number of items to be retrieved
     */
    protected String retmax = "1";

    protected String id;


    /**
     * The NCBI database to be queried.  Values include    
     *  genome
     *  nucleotide
     *  protein
     *  popset
     *  snp
     *  sequences - Composite name including nucleotide, protein, popset and genome.
     */
    protected String db;

    protected String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";


    protected Map inputMap = new HashMap();

    protected Map transmitterMap = new HashMap();

    protected Map outputMap = new HashMap();

    public static final String NEWLINE = System.getProperty("line.separator");

    protected StreamProcessor streamProcessor = null;

    /**
     * This method is responsible for transmitting a map of values, and
     * processing the results
     * 
     * @param transmitterMap
     * @param streamProcessor
     * @throws MalformedURLException
     * @throws TransmitterException
     */
    protected Map transmit(Map transmitterMap) throws MalformedURLException, TransmitterException {
        StreamTransmitter transmitter = new GetStreamTransmitter();
        transmitter.setURL(url);
        //DataThingAdapter adapter = new DataThingAdapter(transmitterMap);
        String outputFile = (String)transmitterMap.get("outputFile");
        String xslt = (String)transmitterMap.get("xslt");
        String retmode = (String)transmitterMap.get("retmode");

        System.out.println("xslt: " + xslt);
        if (retmode != null && retmode.equalsIgnoreCase("xml")) {
            if (xslt == null) {
                streamProcessor = new GetStreamProcessor();
            } else {
                System.out.println("Using XSLT Stream Processor");
                if (outputFile == null) {
                    throw new TransmitterException(
                            "The outputFile attribute was null");
                }
                streamProcessor = new XSLTStreamProcessor((HashMap)outputMap,outputFile, xslt);
            }
        } else {// otherwise treat it as plain text.
            streamProcessor = new GetStreamProcessor();
        }

        return transmitter.transmit(transmitterMap, streamProcessor);
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public abstract Map execute(Map inputMap) throws TaskExecutionException;

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public abstract String[] inputNames();

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public abstract String[] inputTypes();

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[] { "outputText" };
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[] { "'text/plain'" };
    }

}