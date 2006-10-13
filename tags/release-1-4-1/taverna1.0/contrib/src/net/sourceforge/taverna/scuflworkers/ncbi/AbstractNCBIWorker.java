/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.ncbi;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.GetStreamTransmitter;
import net.sourceforge.taverna.io.StreamProcessor;
import net.sourceforge.taverna.io.StreamTransmitter;
import net.sourceforge.taverna.io.TransmitterException;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
/**
 * This class provides basic common functionality needed by all NCBIWorkers.
 * 
 * @author mfortner
 */
public abstract class AbstractNCBIWorker implements LocalWorker {
    
    
    public static final String NEWLINE = System.getProperty("line.separator");

    /**
     * This is the main method of the class.
     * @param inputMap  A map of values to be used by the worker class.
     */
	public abstract Map execute(Map inputMap) throws TaskExecutionException;
	
	/**
	 * This method returns an array of names of input values.  These
	 * values are used as keys in the inputMap.
	 */
	public String[] inputNames() {
	    return new String[]{"term", "maxRecords","outputFile","xslt","ext"};
	}
	
	/**
	 * This method returns an array of input mimetypes.  
	 */
	public String[] inputTypes() {
		return new String[]{"'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'"};
	}
	
	/**
	 * This method returns an array of output value names. These values
	 * are used as keys in the outputMap.
	 */
	public String[] outputNames() {
		return new String[]{"resultsXml"};
	}
	
	
	public String[] outputTypes() {
		return new String[]{"'text/xml'"};
	}
	
	
	protected String fetchData(String url){
		return null;
	}

	protected String rettype;
	protected String term;
	protected String queryKey;
	protected String displayOption;
	protected String originalDb;
	protected int maxRecords=1;
	protected String startTag;
	protected String endTag;
	protected String url= "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";

	protected String cmd;
	protected String cmdCurrent;
	
	protected Map inputMap = new HashMap();
	protected Map transmitterMap = new HashMap();
	protected Map outputMap = new HashMap();
	
	/**
	 * This method is responsible for transmitting a map of values, and processing the results
	 * @param transmitterMap
	 * @param streamProcessor
	 * @throws MalformedURLException
	 * @throws TransmitterException
	 */
	protected Map transmit(Map transmitterMap) throws MalformedURLException, TransmitterException{
		StreamTransmitter transmitter = new GetStreamTransmitter();
		transmitter.setURL(this.url);
		DataThingAdapter adapter = new DataThingAdapter(transmitterMap);
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String outputFile = inAdapter.getString("outputFile");
		String ext = inAdapter.getString("ext");
		System.out.println("ext: " + ext);
		
		System.out.println("======== outputFile: " + outputFile);
		String xslt = adapter.getString("xslt");
		System.out.println("xslt: " + xslt);
		if (xslt == null || xslt.equals("")){
		    streamProcessor = new NCBIXMLStreamProcessor(outputMap, this.startTag, this.endTag, outputFile, ext);
			
		}else {
		    System.out.println("Using XSLT Stream Processor");
		    if (outputFile == null){
		        throw new TransmitterException("The outputFile attribute was null");
		    }
		    streamProcessor = new NCBIXSLTStreamProcessor(outputMap, this.startTag, this.endTag,xslt, outputFile);
		}
		
		return transmitter.transmit(transmitterMap, streamProcessor);
	}

		
	public void setStreamProcessor(StreamProcessor streamProc){
		this.streamProcessor = streamProc;
	}

	
	protected StreamProcessor streamProcessor = null;
	
}