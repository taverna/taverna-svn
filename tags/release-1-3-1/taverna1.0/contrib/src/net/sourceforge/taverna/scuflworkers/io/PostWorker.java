package net.sourceforge.taverna.scuflworkers.io;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.GetStreamProcessor;
import net.sourceforge.taverna.io.GetStreamTransmitter;
import net.sourceforge.taverna.io.StreamProcessor;
import net.sourceforge.taverna.io.StreamTransmitter;
import net.sourceforge.taverna.io.TransmitterException;
import net.sourceforge.taverna.scuflworkers.ncbi.XSLTStreamProcessor;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class POSTs a collection of parameter names/values
 * and returns the resulting document.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput url			The base URL used to fetch the data.
 * @tavinput paramnames		An array of parameter names.
 * @tavinput paramvalues	An array of parameter values.
 * 
 * @tavoutput page			The contents of the page downloaded from the url.
 */
public class PostWorker implements LocalWorker {

    protected Map transmitterMap = new HashMap();

    protected Map outputMap = new HashMap();

    public static final String NEWLINE = System.getProperty("line.separator");

    protected StreamProcessor streamProcessor = null;

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        String[] paramNames = inAdapter.getStringArray("paramnames");
        String[] paramValues = inAdapter.getStringArray("paramvalues");
        String url = inAdapter.getString("url");
        
        
        
        // validate the inputs
        if (url == null || url.equals("")){
            throw new TaskExecutionException("The URL cannot be null or empty"); 
        }
        if (paramNames == null || paramNames.length == 0){
            throw new TaskExecutionException("The paramNames array cannot be null or empty");            
        }
        
        if (paramValues == null || paramValues.length == 0){
            throw new TaskExecutionException("The paramValues array cannot be null or empty");            
        }
        
        if (paramNames.length != paramValues.length){
            throw new TaskExecutionException("The number of parameter values and parameter names must be equal");
        }
        
        // put the inputs into the transmitter map
        for (int i=0; i < paramNames.length; i++){
            transmitterMap.put(paramNames[i],paramValues[i]);
        }
	
		
        try {
            outputMap = this.transmit(transmitterMap, url);
        } catch (MalformedURLException e) {
            System.out.println(url);
           throw new TaskExecutionException(e);
        } catch (TransmitterException e) {
            throw new TaskExecutionException(e);
        } catch (Throwable th){
            throw new TaskExecutionException(th);
        }
		
		return outputMap;
    }
    
    
    /**
     * This method is responsible for transmitting a map of values, and
     * processing the results
     * 
     * @param transmitterMap
     * @param streamProcessor
     * @throws MalformedURLException
     * @throws TransmitterException
     */
    protected Map transmit(Map transmitterMap, String url) throws MalformedURLException, TransmitterException {
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
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"paramnames","paramvalues", "url"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
       return new String[]{"l('text/plain'), l('text/plain')", "'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[]{"page"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
       return new String[]{"text/plain"};
    }

}
