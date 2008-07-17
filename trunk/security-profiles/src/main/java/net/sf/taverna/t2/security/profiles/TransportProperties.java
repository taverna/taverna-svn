package net.sf.taverna.t2.security.profiles;

/**
 * Contains a collection of transport-level security properties 
 * that can be associated with a service.
 * 
 * @author Alexandra Nenadic
 */
public class TransportProperties extends SecurityProperties{
	
	/**
	 *  Transport level properties' constants.
	 */
	public static final String Protocol = "Protocol";
	public static final String Port = "Port";
	public static final String AuthNType = "AuthNType";
	public static final String RequiresClientCert = "RequiresClientCert";
	public static final String ProxyCertDepth = "ProxyCertDepth";
	
	/**
	 * List of PROTOCOLS.
	 */
	public static final String[] PROTOCOLS = {"HTTP/1.0",
			"HTTP/1.1",
			"HTTPS/1.0",
			"HTTPS/1.1"
			};	
	
	/**
	 * List of HTTP authentication types.
	 */	
	public static final String[] HTTP_AUTHENTICATION_TYPES = {"None",
			"Basic",
			"Digest",
			"SPNEGO",
			"OAuth",
			"Kerberos"
			};
	/**
	 * List of proxy certificate depths.
	 */
	public static final String[] PROXY_CERT_DEPTHS = {"0", 
		"Infinite", 
		"1", 
		"2", 
		"3", 
		"4", 
		"5", 
		"6", 
		"7", 
		"8", 
		"9", 
		"10"
		};
	
	
	
	public TransportProperties(){
		
		super();
		setProperty(TransportProperties.Protocol, null);
		setProperty(TransportProperties.Port, null);
		setProperty(TransportProperties.AuthNType, null);
		setProperty(TransportProperties.RequiresClientCert, null);
		setProperty(TransportProperties.ProxyCertDepth, null);
	}

}
