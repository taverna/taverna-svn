package net.sourceforge.taverna.io;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.iharder.Base64;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;



/**
 * This class uses an HTTP GET to send data to a URL endpoint, and returns
 * the result
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public class GetStreamTransmitter extends AbstractStreamTransmitter {

    /**
     * @see net.sourceforge.taverna.io.StreamTransmitter#transmit(java.util.Map, net.sourceforge.taverna.io.StreamProcessor)
     */
    public Map transmit(Map map, StreamProcessor streamProcessor)
            throws TransmitterException {
        Map outputMap = new HashMap();
        try {
            //String path = context;
            //path = (serviceName != null)?path+ "/" +serviceName:path;
            String path =  this.makeUrl(map);
            System.out.println("\n\npath: "+path);
            
            GetMethod method = new GetMethod(path);
            
            
            method.setFollowRedirects(true);

            Iterator dataIterator = map.keySet().iterator();

            if (dataIterator != null) {
                //Set Authentication Headers
                if ((userName != null) || (password != null)) {
                    method.addRequestHeader("Authorization",
                            "Basic " +
                            Base64.encodeString(userName + ":" + password));
                }

                //Set mime Headers
                if (mimeHeaders != null && mimeHeaders.keySet() != null ) {
                    Iterator mimeIter = mimeHeaders.keySet().iterator();

                    while (mimeIter.hasNext()) {
                        String mimeParamName = (String) mimeIter.next();
                        method.addRequestHeader(mimeParamName,
                                (String) mimeHeaders.get(mimeParamName));
                    }
                }
            }
            
	    HostConfiguration config = new HostConfiguration();
	    config.setHost(host, port);
            HttpClient client = new HttpClient();
	    client.setHostConfiguration(config);
            //client.startSession(host, port);
            client.executeMethod(method);
            
            outputMap = streamProcessor.processStream(method.getResponseBodyAsStream());
            
            int statusCode = method.getStatusCode();
            int statusLevel = statusCode / 100;
            

            // if there is an invalid response code from the service, throw a TransmitterException
            if ((statusLevel != 2)) {               
                TransmitterException ex = new TransmitterException(method.getStatusText());
                throw ex;
            }
        } catch (HttpException httpe) {
            httpe.printStackTrace();
            throw new TransmitterException(httpe);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new TransmitterException(ioe);
        } catch (Exception th){
            throw new TransmitterException(th);
        }

        return outputMap;
    }
    
    /**
     * This method creates the URL for the transmitter.
     * @param reflector  The reflector containing the data to be sent.
     * @return
     * @throws TransmitterException  If the URL length exceeds 256 chars.
     * @throws IllegalArgumentException if the reflector was null or contained no values.
     */
    protected String makeUrl(Map map) throws TransmitterException {
        

        if ((map == null) || map.isEmpty()) {
            throw new IllegalArgumentException(
                    "The reflector was null or there were no parameters in it.");
        }
        
        Iterator parmIt = map.keySet().iterator();

        StringBuffer sb = new StringBuffer(256);
        sb.append(this.url);
        sb.append("?");

        String currKey = null;
        String currVal = null;
        int count = 0;

        while (parmIt.hasNext()) {
            

            currKey = (String) parmIt.next();
            currVal = (String)map.get(currKey);
            if (currVal == null){
                continue;
            }
            if (count > 0) {
                sb.append("&");
            }
            sb.append(makeParameter(currKey, currVal));
            count++;
        }

        // If the url exceeds 256 characters, throw an exception.
        if (sb.length() > 256) {
            throw new TransmitterException("URL Exceeds 256 chars");
        }

        return sb.toString();
    }
    
    /**
     * This method makes a parameter value pair
     * @param name   The name of the parameter.
     * @param value  The value of the parameter.
     * @return  A StringBuffer in the form "parameterName=value".  To keep the
     *          StringBuffer->String conversion
     *          overhead down, this method returns a StringBuffer.
     */
    public StringBuffer makeParameter(String name, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("=");
        sb.append(URLEncoder.encode(value));

        return sb;
    }

}
