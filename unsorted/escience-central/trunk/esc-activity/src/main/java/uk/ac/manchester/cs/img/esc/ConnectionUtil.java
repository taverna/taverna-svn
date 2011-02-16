package uk.ac.manchester.cs.img.esc;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import com.connexience.server.api.API;
import com.connexience.server.api.APIConnectException;
import com.connexience.server.api.APIFactory;
import com.connexience.server.api.APIInstantiationException;
import com.connexience.server.api.APIParseException;
import com.connexience.server.api.APISecurityException;
import com.connexience.server.api.impl.InkspotTypeRegistration;

public class ConnectionUtil {
	
	private static String APPLICATION_ID = "402880852de18993012e29a8df791a0f";
	private static String APPLICATION_KEY = "f48af94e2e46e710c5f3e266cb34630e";
	
	
	public static API getAPI(String urlString) throws APIConnectException, MalformedURLException, APISecurityException, APIParseException, APIInstantiationException, CMException {
		InkspotTypeRegistration.register();
        APIFactory factory = new APIFactory();
        factory.setApiClass(com.connexience.client.api.impl.HttpClientAPI.class);

        API api = factory.authenticateApplication(new URL(urlString), APPLICATION_ID, APPLICATION_KEY);
        
        CredentialManager cm = getCredentialManager();
        
        String[] userAndPass = cm.getUsernameAndPasswordForService(urlString);
        
        api.authenticate(userAndPass[0], userAndPass[1]);
        return api;
		
	}
	
	private static CredentialManager getCredentialManager() throws CMException {
		return CredentialManager.getInstance();
	}

}
