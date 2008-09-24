package org.embl.ebi.escience.lsidauth;

// This file is a component of the LSIDAuth project,
// and is licensed under the GNU LGPL.
// Copyright Tom Oinn, EMBL-EBI

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.io.UnsupportedEncodingException;
import javax.security.auth.login.FailedLoginException;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import com.ibm.lsid.LSID;
import com.ibm.lsid.client.LSIDResolver;
import com.ibm.lsid.wsdl.WSDLConstants;
import com.ibm.lsid.client.metadata.LSIDMetadata;
import com.ibm.lsid.wsdl.LSIDMetadataPort;
import com.ibm.lsid.wsdl.LSIDWSDLWrapper;
import com.ibm.lsid.MetadataResponse;
import java.io.InputStream;

/**
 * Final class providing access to password generation
 * and verification.
 * @author Tom Oinn
 * @author Victor Tan
 */
public final class LSIDAuth implements WSDLConstants {
    

    /**
     * Initialize the instance with the specified HMAC. This
     * method MUST be called before any attempts to reference
     * an instance with the getInstance() method, failure to
     * do so will cause a RuntimeException in the getInstance()
     * call. If the instance is already initialized this will
     * clear the cache and reset the 
     */
    public static void init(byte[] keyBytes) {
	synchronized(lock) {
	    if (LSIDAuth.instance != null) {
		// Set the isValid flag on this instance to false
		// and create a new instance in its place
		LSIDAuth.instance.isValid = false;
	    }
	    LSIDAuth.instance = new LSIDAuth(keyBytes);
	    LSIDAuth.cache = new HashMap();
	}
    }
    

    /**
     * Get a reference to the current LSIDAuth instance. If this is called
     * before a call to LSIDAuth.init() this will throw a RuntimeException
     */
    public static LSIDAuth getInstance() {
	if (LSIDAuth.instance == null) {
	    throw new RuntimeException("You must call LSIDAuth.init(byte[]) "+
				       "before attempting to obtain an instance!");
	}
	else {
	    return LSIDAuth.instance;
	}
    }
    

    /**
     * Given a plaintext password this returns
     * a byte[] containing an encrypted password
     * which will then authenticate using the
     * comparePasswords method.
     * @return String containing the base64 encoded encrypted password   
     * @throws NoSuchAlgorithmException if the provider is not correctly installed
     * @throws InvalidKeyException if the key is invalid for some reason
     */
    public String createPassword(String password) 
	throws NoSuchAlgorithmException, 
	       InvalidKeyException {
	// Check that this instance is still valid
	checkValid();
	SecureRandom random = new SecureRandom();
	byte[] salt = new byte[SIZE_OF_SALT];
	random.nextBytes(salt);
	SecretKeySpec key = new SecretKeySpec(this.keyBytes, MAC_METHOD);
	Mac mac = Mac.getInstance(MAC_METHOD);
	mac.init(key);
	mac.update(salt);
	try {
	    mac.update(password.getBytes("UTF8"));
	}
	catch (UnsupportedEncodingException uee) {
	    // Never happens, all platforms support UTF8
	}
	byte[] digest = mac.doFinal();
	
	byte[] result = new byte[salt.length + digest.length];
	System.arraycopy(salt, 0, result, 0, salt.length);
	System.arraycopy(digest, 0, result, salt.length, digest.length);
	
	return Base64.encodeBytes(result);
    }
    
    
    /**
     * Verify the supplied user / password pair against the backing
     * password store or cache as appropriate. The time parameters
     * are specified in milliseconds.
     * @param personLSID the LSID of the Person to authenticate
     * @param password the String plaintext password to verify
     * @param cacheExpiry time in milliseconds before the lookup is
     * removed from the cache, timed from its creation.
     * @param sessionExpiry time in milliseconds from the last lookup
     * of this entry until it is expired from the cache.
     * @throws FailedLoginException if the password does not match,
     * the user is not found or some other problem occurs. If there was
     * a failure in the underlying system more detail will be available
     * from the getCause() method on the exception object.
     */
    public void login(String personLSID, String password, 
		      long cacheExpiry, long sessionExpiry) 
	throws FailedLoginException {
	// Check this instance is still valid
	checkValid();
	// Try to fetch from the cache first
	byte[] hashPassword = get(personLSID, cacheExpiry, sessionExpiry);
	if (hashPassword == null) {
	    // Return was null so we have to fetch from the authority
	    try {
		hashPassword = getFromLSID(personLSID);
		// Got the password so store it in the cache
		put(personLSID, hashPassword);
	    }
	    catch (Exception ex) {
		// Something didn't work, fail the login and add
		// the underlying exception as the cause for the
		// FailedLoginException.
		FailedLoginException fle = new FailedLoginException("Unable to fetch data from authority for "+personLSID);
		fle.initCause(ex);
		throw fle;
	    }
	}
	// Now have the hash password, we already have the secret
	// key and the cleartext password so...
	try {
	    if (comparePasswords(hashPassword, password) == true) {
		return;
	    }
	}
	catch (NoSuchAlgorithmException nsae) {
	    FailedLoginException fle = new FailedLoginException("No such encryption algorithm, check your provider "+
								"installation!");
	    fle.initCause(nsae);
	    throw fle;
	}
	catch (InvalidKeyException ike) {
	    FailedLoginException fle = new FailedLoginException("Invalid key exception in security module.");
	    fle.initCause(ike);
	    throw fle;
	}
	FailedLoginException fle = new FailedLoginException("Invalid password for "+personLSID);
	throw fle;
    }


    /**
     * Purge all entries from the cache
     */
    public void purgeCache() {
	checkValid();
	synchronized(lock) {
	    cache.clear();
	}
    }


    // ****************************************************************************
    // internal members below this point
    // ****************************************************************************

    /** Size of initial salt in bytes */
    private static final int SIZE_OF_SALT = 12;
    
    /** Crypto algorithm to use */
    private static final String MAC_METHOD = "HMACSHA1";
    
    /**
     * The singleton instance, used primarily to hold
     * the cache of encrypted passwords
     */
    private static LSIDAuth instance = null;
    
    /** The HMAC used by instances */
    private byte[] keyBytes;

    /**
     * Flag to denote whether this instance is valid, this
     * is cleared on any calls to LSIDAuth.init() in order
     * to prevent an out of date instance doing anything it
     * shouldn't.
     */
    private boolean isValid = true;

    /** Cache of LSID -> [cpass, creationtime, accesstime] */
    private static Map cache = null;

    /** Used to provide synchronization over the instance */
    private static final byte[] lock = "foo".getBytes();

    // ****************************************************************************
    // Internal methods below this point
    // ****************************************************************************


    /**
     * Create a new instance of the LSIDAuth and set the secret
     * key to that supplied in the constructor
     */
    private LSIDAuth(byte[] keyBytes) {
	this.keyBytes = keyBytes;
    }

    
    /**
     * Fetch the metadata for the specified LSID and parse it
     * to locate the appropriate description element containing
     * the encrypted password
     * @param personLSID the LSID to query
     * @return byte[] containing the encrypted password, actually
     * fetches a base64 endoded string from the authority and decodes
     * it before returning.
     * @throws Exception passed back to the calling method
     * as it's always going to be wrapped up in another
     * exception with this as the cause, nothing else should
     * be calling this method!
    */
    private byte[] getFromLSID(String personLSID) 
	throws Exception {
	LSID lsid = new LSID(personLSID);
	LSIDResolver resolver = new LSIDResolver(lsid);
	LSIDWSDLWrapper wrapper = resolver.getWSDLWrapper();
	LSIDMetadataPort port = wrapper.getMetadataPortForProtocol(HTTP);
	MetadataResponse resp = resolver.getMetadata(port);
	InputStream in = resp.getMetadata();
	// Build a JDOM Document from this input stream
	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(in);
	// Now have an XML document, which hopefully contains some
	// RDF which we can parse to extract the person's password
	// from.
	Element rootElement = doc.getRootElement();
	List descriptions = rootElement.getChildren("description",rdfNamespace);
	Element passwordElement = null;
	for (Iterator i = descriptions.iterator(); 
	     i.hasNext() && passwordElement == null;) {
	    Element description = (Element)i.next();
	    String about = description.getAttributeValue("about",rdfNamespace);
	    if (about != null &&
		about.equals(personLSID)) {
		passwordElement = description.getChild("password",mygridNamespace);
	    }
	}
	if (passwordElement == null) {
	    throw new RuntimeException("Cannot find password element!");
	}
	return Base64.decode(passwordElement.getTextTrim());
    }
    private static Namespace rdfNamespace = 
	Namespace.getNamespace("rdf",
			       "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static Namespace mygridNamespace =
	Namespace.getNamespace("j.0",
			       "http://www.mygrid.org.uk/2004/infomodel-rdf/0.1#");

        
    /**
     * Verifies the supplied cleartext password against the
     * supplied encrypted password using the previously
     * initialized secret key.
     * @return true if password is valid, false otherwise
     * @throws NoSuchAlgorithmException if the provider is not correctly installed
     * @throws InvalidKeyException if the key is invalid for some reason
     */
     boolean comparePasswords(byte[] hashPassword, String stringPassword)
	throws NoSuchAlgorithmException, 
	       InvalidKeyException {
	byte[] salt = new byte[SIZE_OF_SALT];
	byte[] origDigest = new byte[hashPassword.length - SIZE_OF_SALT];
	
	System.arraycopy (hashPassword, 0, salt, 0, SIZE_OF_SALT);
	System.arraycopy (hashPassword, SIZE_OF_SALT, origDigest, 0, 
			  hashPassword.length - SIZE_OF_SALT);
	SecretKeySpec key = new SecretKeySpec(this.keyBytes, MAC_METHOD);
	Mac mac = Mac.getInstance(MAC_METHOD);
	mac.init(key);
	mac.update(salt);
	try {
	    mac.update(stringPassword.getBytes("UTF8"));
	}
	catch (UnsupportedEncodingException uee) {
	    // Never happens, all platforms support UTF8
	}
	byte[] digest = mac.doFinal();
	return Arrays.equals(digest, origDigest);
    }


    /**
     * Check that the isValid flag is set to true, throw 
     * an exception otherwise
     */
    private void checkValid() {
	if (!isValid) {
	    throw new RuntimeException("LSIDAuth instance has expired due to a subsequent call to init().");
	}
    }


    /**
     * Try to fetch an encrypted password from the cache, if the
     * password is not stored or either of the times have expired
     * then return null otherwise return the byte[] of the
     * encrypted password.
     * @param personLSID the key in the cache to look up
     * @param sessionTimeout the maximum number of milliseconds since the
     * last access to the cache entry before the entry is declared invalid
     * @param cacheTimeout the maximum number of milliseconds since the 
     * creation of the entry before it is declared invalid
     * @return byte[] containing the encrypted password or null if it
     * is either not found or has expired.
     */
    private byte[] get(String personLSID, long cacheTimeout, long sessionTimeout) {
	synchronized(lock) {
	    CacheEntry entry = (CacheEntry)cache.get(personLSID);
	    if (entry == null) {
		return null;
	    }
	    // Entry wasn't null, check the date ranges
	    long currentTime = new Date().getTime();
	    if (entry.getCreationTime().getTime() + cacheTimeout > currentTime ||
		entry.getLastAccessTime().getTime() + sessionTimeout > currentTime) {
		// Entry has expired
		cache.remove(personLSID);
		return null;
	    }
	    else {
		return entry.getPasswordAndTouch();
	    }	
	}
    }


    /**
     * Insert a new entry into the cache
     */
    private void put(String personLSID, byte[] hashPassword) {
	synchronized(lock) {
	    CacheEntry entry = new CacheEntry(hashPassword);
	    if (cache.containsKey(personLSID) == false) {
		cache.put(personLSID, entry);
	    }
	}
    }


    /**
     * A bean to contain the access time information and
     * the byte[] containing the encrypted password.
     */
    private class CacheEntry {
	private Date creationTime;
	private Date lastAccessTime;
	private byte[] bytes;
	protected CacheEntry(byte[] hashPassword) {
	    creationTime = new Date();
	    lastAccessTime = new Date();
	    bytes = hashPassword;
	}
	protected Date getLastAccessTime() {
	    return lastAccessTime;
	}
	protected Date getCreationTime() {
	    return creationTime;
	}
	protected byte[] getPasswordAndTouch() {
	    lastAccessTime = new Date();
	    return bytes;
	}
    }

}
