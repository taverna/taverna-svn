package net.sf.taverna.t2.security.agents;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Vector;

import net.sf.taverna.t2.security.requests.SecurityRequest;
import net.sf.taverna.t2.security.requests.WSSecurityRequest;

/**
 * 
 * @author Alexandra Nenadic
 */
public class SecurityAgentManager {

    /** Keystore containing user's passwords, private keys and public key certificate chains*/
    private static KeyStore keystore;
    
    /** Truststore containing trusted certificates of CA authorities and servers. */
    private static KeyStore truststore;
          
    /** A map of service URLs associated with private key aliases, 
     * i.e. aliases are keys in the hasmap and lists of URLs are
     * hashmap values.
     */
    private static HashMap<String, Vector<String>> serviceURLs;

	
	/**
	 * Constructs SecurityAgentManager.
	 */
	public SecurityAgentManager(KeyStore keyStr, HashMap<String, Vector<String>> servURLs, KeyStore trusyStr){
		
		keystore = keyStr;
		serviceURLs = servURLs;
		truststore = trusyStr;
	}
	
	/**
	 * Gets an appropriate agent for the security request (e.g. if the request is for a WS,
	 * the returned agent will know how to perform the required WS-Security operations).
	 * @param secReq
	 * @return
	 */
	public SecurityAgent getSecurityAgent(SecurityRequest secReq){
		
		if (secReq instanceof WSSecurityRequest){
			return new WSSecurityAgent(((WSSecurityRequest) secReq).getServiceURL(), keystore, serviceURLs, truststore);
		}
		else{
			return null;
		}
	}
	
	/**
	 * Can we and are we willing to provide an agent to answer the security request?
	 * @param secReq
	 * @return
	 */
	public boolean canProvideSecurityAgent(SecurityRequest secReq){
		if (secReq instanceof WSSecurityRequest){
			/* If we have the proper credential for the request 
			 * (e.g. if the request is for Username Token we must have a password, if 
			 * the request is for signing, we must have the private key pair) 
			 * and the user's internal policy allows it - return true, else return false
			 */
			return true;
		}
		else{	
			return false;
		}
	}
	
	/*public boolean dummy(){
		return true;
	}*/
}

