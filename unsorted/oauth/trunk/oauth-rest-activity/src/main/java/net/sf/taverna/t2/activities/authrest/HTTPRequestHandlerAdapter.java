package net.sf.taverna.t2.activities.authrest;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * HTTPRequestHandlerAdapter is an interface. 
 * 
 * @author Mark Borkum
 * @see HTTPRequestHandler
 */
public interface HTTPRequestHandlerAdapter {

	/**
	 * Finalize the HTTP request before it is sent. 
	 * 
	 * @param httpRequest  the HTTP request.
	 */
	public void finalize(HttpRequestBase httpRequest);
	
	/**
	 * Modify the URL before the HTTP request is constructed.
	 *  
	 * @param httpRequestUrl  the original URL.
	 * @return  the modified URL.
	 */
	public String modify(String httpRequestUrl);
	
}
