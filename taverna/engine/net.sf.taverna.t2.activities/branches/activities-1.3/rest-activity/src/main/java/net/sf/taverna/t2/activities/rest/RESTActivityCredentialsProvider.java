package net.sf.taverna.t2.activities.rest;

import java.net.URI;
import java.net.URLEncoder;
import java.security.Principal;

import javax.management.remote.JMXPrincipal;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
//import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.log4j.Logger;

/**
 * This CredentialsProvider acts as a mediator between the Apache HttpClient
 * and Taverna's CredentialManager that stores all user's credentials.
 * 
 * The only role of it is to retrieve stored details from CredentialManager
 * when they are required for HTTP authentication.
 * 
 * @author Sergejs Aleksejevs
 * @author Alex Nenadic
 */
public class RESTActivityCredentialsProvider extends BasicCredentialsProvider
{
  private static Logger logger = Logger.getLogger(RESTActivityCredentialsProvider.class);
  
  private static final int DEFAULT_HTTP_PORT = 80;
  private static final int DEFAULT_HTTPS_PORT = 443;
  
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";
  
  
  // the only existing instance of this class - it's a singleton
  private static RESTActivityCredentialsProvider credentialsProvider;
  
  
  private RESTActivityCredentialsProvider() {
    // making constructor private - nobody can now instantiate this class manually
  }
  
  /**
   * @return The only existing instance of the RESTActivityCredentialsProvider - singleton.
   */
  public static RESTActivityCredentialsProvider getInstance()
  {
    if (credentialsProvider == null) {
      synchronized (RESTActivityCredentialsProvider.class) {
        if (credentialsProvider == null) {
          credentialsProvider = new RESTActivityCredentialsProvider();
        }
      }
    }
    return (credentialsProvider);
  }
  
  @Override
  public Credentials getCredentials(AuthScope authscope)
  {
    logger.info("Looking for credentials for: Host - " + authscope.getHost() + ";" +
        "Port - " + authscope.getPort() + ";" + 
        "Realm - " + authscope.getRealm() + ";" +
        "Authentication scheme - " + authscope.getScheme());
    
	// Ask the superclass first
    Credentials creds = super.getCredentials(authscope);
    if (creds != null){ 
    	// We have used setCredentials() on this class (for proxy host, port, username,password) 
    	// just before we invoked the http request, which will then pick the proxy credentials up from here.
    	return creds; 
    }
    
    // Otherwise, ask Credential Manager if is can provide the credential
    
    String AUTHENTICATION_REQUEST_MSG = "This REST service requires authentication in " + authscope.getRealm();
    
    try {
      CredentialManager credManager = CredentialManager.getInstance();
      UsernamePassword credentials = null;
      
      /*
       * if port is 80 - use HTTP, don't append port
       * if port is 443 - use HTTPS, don't append port
       * any other port - append port + do 2 tests:
       * --- test HTTPS first  has...()
       * --- if not there, do get...() for HTTP (which will save the thing)
       * (save both these entries for HTTP + HTTPS if not there)
       * 
       */
      
      // build the service URI back to front 
      StringBuilder serviceURI = new StringBuilder();
      serviceURI.insert(0, "/#" + URLEncoder.encode(authscope.getRealm(), "UTF-16"));
      if (authscope.getPort() != DEFAULT_HTTP_PORT && authscope.getPort() != DEFAULT_HTTPS_PORT) {
        // non-default port - add port name to the URI
        serviceURI.insert(0, ":" + authscope.getPort());
      }
      serviceURI.insert(0, authscope.getHost());
      serviceURI.insert(0, "://");
      
      
      // now the URI is complete, apart from the protocol name
      if (authscope.getPort() == DEFAULT_HTTP_PORT || authscope.getPort() == DEFAULT_HTTPS_PORT)
      {
        // definitely HTTP or HTTPS
        serviceURI.insert(0, (authscope.getPort() == DEFAULT_HTTP_PORT ? HTTP_PROTOCOL : HTTPS_PROTOCOL));
        
        // request credentials from CrendentialManager
        credentials = credManager.getUsernameAndPasswordForService(
            URI.create(serviceURI.toString()), true, AUTHENTICATION_REQUEST_MSG);
      }
      else {
        // non-default port - will need to try both HTTP and HTTPS;
        // just check (no pop-up will be shown) if credentials are there - one protocol that
        // matched will be used; if 
        if (credManager.hasUsernamePasswordForService(URI.create(HTTPS_PROTOCOL + serviceURI.toString()))) {
          credentials = credManager.getUsernameAndPasswordForService(
              URI.create(HTTPS_PROTOCOL + serviceURI.toString()), true, AUTHENTICATION_REQUEST_MSG);
        }
        else if (credManager.hasUsernamePasswordForService(URI.create(HTTP_PROTOCOL + serviceURI.toString()))) {
          credentials = credManager.getUsernameAndPasswordForService(
              URI.create(HTTP_PROTOCOL + serviceURI.toString()), true, AUTHENTICATION_REQUEST_MSG);
        }
        else {
          // non of the two options succeeded, request details with a popup for HTTP...
          credentials = credManager.getUsernameAndPasswordForService(
              URI.create(HTTP_PROTOCOL + serviceURI.toString()), true, AUTHENTICATION_REQUEST_MSG);
          
          // ...then save a second entry with HTTPS protocol (if the user has chosen to save the credentials)
          if (credentials != null && credentials.isShouldSave()) {
            credManager.saveUsernameAndPasswordForService(credentials, URI.create(HTTPS_PROTOCOL + serviceURI.toString()));
          }
        }
      }
      
      
      if (credentials != null) {
        logger.info("Credentials obtained successfully");
        return new RESTActivityCredentials(credentials.getUsername(), credentials.getPasswordAsString());
      }
    }
    catch (Exception e) {
      logger.error("Unexpected error while trying to obtain user's credential from CredentialManager", e);
    }
    
    // error or nothing was found
    logger.info("Credentials not found - the user must have refused to enter them.");
    return null;
  }
  
  /**
   * This class encapsulates user's credentials that this CredentialsProvider can
   * pass to Apache HttpClient.
   * 
   * @author Sergejs Aleksejevs
   */
  public class RESTActivityCredentials implements Credentials
  {
    // this seems to be the simplest existing standard implementation of Principal interface
    private final JMXPrincipal user;
    private final String password;

    public RESTActivityCredentials(String username, String password) {
      this.user = new JMXPrincipal(username);
      this.password = password;
    }

    public String getPassword() {
      return password;
    }

    public Principal getUserPrincipal() {
      return user;
    }
    
  }
  

}
