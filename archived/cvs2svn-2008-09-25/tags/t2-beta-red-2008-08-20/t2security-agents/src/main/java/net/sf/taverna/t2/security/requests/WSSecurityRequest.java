package net.sf.taverna.t2.security.requests;

import net.sf.taverna.t2.security.profiles.WSSecurityProfile;

/**
 * 
 * @author Alexandra Nenadic *
 */
public class WSSecurityRequest extends SecurityRequest{
	
	/**
	 * URL of the service WSDL's location, 
	 * used to identify the credential in the Keystore to use for this service.  
	 */
	private String serviceURL;
	
	/**
	 * Security profile for the WS, which contains security requirements of the WS, i.e.
	 * what security actions are required oin order to invoke this WS.
	 */
	private WSSecurityProfile wsSecProfile;

	/**
	 * Constructor
	 * @param wsdlURL
	 * @param wssecprof
	 */
	public WSSecurityRequest(String wsdlURL, WSSecurityProfile wssecprof){
		
		serviceURL = wsdlURL;
		wsSecProfile =  wssecprof;

	}
	
	/**
	 * Gets the service's WSDL URL.
	 * @return
	 */
	public String getServiceURL(){
		return serviceURL;
	}
	
	/**
	 * Gets the service's security profile.
	 * @return
	 */
	public WSSecurityProfile getSecurityProfileL(){
		return wsSecProfile;
	}
}
