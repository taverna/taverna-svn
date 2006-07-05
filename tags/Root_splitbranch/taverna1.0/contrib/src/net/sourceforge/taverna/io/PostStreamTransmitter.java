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
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * This class uses an HTTP POST method to transmit data to a URL,
 * and to return the results in a map.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.4 $
 */
public class PostStreamTransmitter extends AbstractStreamTransmitter {

    /**
     * 
     */
    public Map transmit(Map map, StreamProcessor streamProcessor)
            throws TransmitterException {
        Map outputMap = new HashMap();
        try {
            String path = context;
            path = (serviceName != null)?path+ "/" +serviceName:path;
            PostMethod method = new PostMethod(path);
            
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

                //Set Data Params
                while (dataIterator.hasNext()) {
                    String paramName = (String) dataIterator.next();
                    String paramValue = (String)map.get(paramName);
                    paramValue = (paramValue == null) ? "" : paramValue;
                    method.addParameter(paramName,
                            URLEncoder.encode(paramValue, "UTF-8"));
                }
            }

	    HostConfiguration config = new HostConfiguration();
	    config.setHost(host, port);
            HttpClient client = new HttpClient();
	    client.setHostConfiguration(config);
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
            throw createTxException(httpe);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw createTxException(ioe);
        } catch (Exception th){
            throw createTxException(th);
        }

        return outputMap;

    }

}
