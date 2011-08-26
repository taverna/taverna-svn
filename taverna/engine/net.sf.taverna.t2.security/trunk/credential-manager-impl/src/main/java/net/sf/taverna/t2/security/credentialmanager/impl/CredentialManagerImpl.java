/*******************************************************************************
 * Copyright (C) 2008-2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.security.credentialmanager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CMUtils;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.JavaTruststorePasswordProvider;
import net.sf.taverna.t2.security.credentialmanager.KeystoreChangedEvent;
import net.sf.taverna.t2.security.credentialmanager.MasterPasswordProvider;
import net.sf.taverna.t2.security.credentialmanager.ServiceUsernameAndPasswordProvider;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmationProvider;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Provides an implementation of {@link #CredentialManagerService}.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 */

public class CredentialManagerImpl implements CredentialManager,
		Observable<KeystoreChangedEvent> {

	// Various passwords to try for the Java's default truststore.
	public static List<String> defaultTrustStorePasswords = Arrays.asList(
			System.getProperty(
					CredentialManager.PROPERTY_TRUSTSTORE_PASSWORD, ""),
			"changeit", "changeme", "");

	// For Taverna 2.2 and older - Keystore was BC-type with user-set password
	// and Truststore was JKS-type with the default password
	public static final String OLD_TRUSTSTORE_PASSWORD = "Tu/Ap%2_$dJt6*+Rca9v";
	public static final String OLD_T2TRUSTSTORE_FILE = "t2truststore.jks";

	private static Logger logger = Logger
			.getLogger(CredentialManagerImpl.class);

	// Multicaster of KeystoreChangedEventS
	private MultiCaster<KeystoreChangedEvent> multiCaster = new MultiCaster<KeystoreChangedEvent>(
			this);

	// A directory containing Credential Manager's Keystore/Truststore/etc.
	// files.
	private File credentialManagerDirectory = null;

	// Master password for Credential Manager - used to create/access the
	// Keystore and Truststore.
	private String masterPassword;

	// Keystore file
	private File keystoreFile = null;

	// Truststore file
	private File truststoreFile = null;

	// Keystore containing user's passwords and private keys with corresponding
	// public key certificate chains.
	private KeyStore keystore;

	// Truststore containing trusted certificates of CA authorities and services
	// (servers).
	private KeyStore truststore;

	// Has the Credential Manager been initialized (i.e. the Keystore/Truststore
	// loaded, etc.)
	private boolean isInitialized = false;

	// Whether SSLSocketFactory has been initialised with Taverna's
	// Keystore/Truststore.
	// Actually tavernaSSLSocketFactory==null? check tells us if 
	// Taverna's SSLSocketFactory has been initialised
	//private static boolean sslInitialized = false;

	private static SSLSocketFactory tavernaSSLSocketFactory;

	// Observer of changes to the Keystore and Truststore that
	// updates the default SSLContext and SSLSocketFactory at the single
	// location rather than
	// all over the code when changes to the keystores occur.
	private KeystoreChangedObserver keystoresChangedObserver = new KeystoreChangedObserver();

	// Cached list of all services that have a username/password entry in the
	// Keystore
	private List<URI> cachedServiceURIsList = null;

	// Cached map of all URI fragments to their original URIs for services that
	// have a username/password
	// entry in the Keystore. This is normally used to recursively discover the
	// realm of the service
	// for HTTP authentication so we do not have to ask user for their username
	// and password for
	// every service in the same realm.
	private HashMap<URI, URI> cachedServiceURIsMap = null;

	// Observer that clears the above list and map on any change to the Keystore
	private ClearCachedServiceURIsObserver clearCachedServiceURIsObserver = new ClearCachedServiceURIsObserver();

	// A list of master password providers
	private List<MasterPasswordProvider> masterPasswordProviders;

	// A list of Java truststore password (used to encrypt/decrypt the Java's
	// default truststore) providers
	private List<JavaTruststorePasswordProvider> javaTruststorePasswordProviders;

	// A list of providers of usernames and passwords for services
	private List<ServiceUsernameAndPasswordProvider> serviceUsernameAndPasswordProviders;

	// A list of providers of trust confirmation for services
	private List<TrustConfirmationProvider> trustConfirmationProviders;

	public CredentialManagerImpl() throws CMException {

		// Make sure we have BouncyCastle provider installed, just in case
		// (needed for some tests and reading PKCS#12 keystores)
		Security.addProvider(new BouncyCastleProvider());
		
		// Open the files stored in the (DEFAULT!!!) Credential Manager's
		// directory
		// loadDefaultConfigurationFiles();
		// FIXME
		// Get the location of the directory containing the Keystore and
		// Truststore somehow - from OSGi's Configuration Service

		// initialize();
	}

	/**
	 * Initialize Credential Manager - load the Keystore and Truststore.
	 */
	private void initialize() throws CMException {

		// Only do this if the Credential Manager has not been initialized so
		// far
		if (!isInitialized) {
			masterPassword = getMasterPassword();

			this.addObserver(clearCachedServiceURIsObserver);
			this.addObserver(keystoresChangedObserver);

			// Load the Keystore
			try {
				loadKeystore();
				logger.info("Credential Manager: Loaded the Keystore.");
			} catch (CMException cme) {
				isInitialized = false;
				masterPassword = null; // just in case we need to try again
				// logger.error(cme.getMessage(), cme);
				throw cme;
			}

			// Load the Truststore
			try {
				loadTruststore();
				logger.info("Credential Manager: Loaded the Truststore.");
			} catch (CMException cme) {
				isInitialized = false;
				masterPassword = null; // just in case we need to try again
				// logger.error(cme.getMessage(), cme);
				throw cme;
			}

			isInitialized = true;
		}
	}

	/**
	 * Get the master password from the available providers.
	 * 
	 * @return master password
	 * @throws CMException
	 *             if none of the providers can provide a non-null master
	 *             password
	 */
	private String getMasterPassword() throws CMException {

		if (masterPassword != null) {
			return masterPassword;
		}

		if (keystoreFile == null) {
			loadDefaultConfigurationFiles();
		}

		boolean firstTime = !keystoreFile.exists();
		
//		if (keystoreFile != null && !keystoreFile.exists()){
//			firstTime = true;
//		}
//		else if (credentialManagerDirectory.exists()){
//			File ksFile = new File (credentialManagerDirectory, TRUSTSTORE_FILE_NAME);
//			if (!ksFile.exists()){
//				firstTime = true;
//			}
//		}

		for (MasterPasswordProvider masterPasswordProvider : masterPasswordProviders) {
			String password = masterPasswordProvider
					.getMasterPassword(firstTime);
			if (password != null) {
				return password;
			}
		}

		// We are in big trouble - we do not have a single master password
		// provider.
		String exMessage = "Failed to obtain master password from providers: "
				+ masterPasswordProviders;
		logger.error(exMessage);
		throw new CMException(exMessage);
	}

	/**
	 * Load Taverna's Keystore from a file on the disk.
	 */
	private void loadKeystore() throws CMException {

		if (keystore == null) {
			try {
				// Try to create Taverna's Keystore as Bouncy Castle UBER-type
				// keystore.
				keystore = KeyStore.getInstance("UBER", "BC");
			} catch (Exception ex) {
				// The requested keystore type is not available from security
				// providers.
				String exMessage = "Failed to instantiate Taverna's Keystore.";
				throw new CMException(exMessage, ex);
			}

			if (keystoreFile.exists()) { // If the file exists, open it

				// Try to load the Keystore
				FileInputStream fis = null;
				try {
					// Get the file
					fis = new FileInputStream(keystoreFile);
					// Load the Keystore from the file
					keystore.load(fis, masterPassword.toCharArray());
				} catch (Exception ex) {
					keystore = null; // make it null as it was just created but
										// failed to load so it is not null
					masterPassword = null; // it is probably the wrong password
											// so do not remember it just in
											// case
					String exMessage = "Failed to load Taverna's Keystore. Possible reason: incorrect password or corrupted file.";
					logger.error(exMessage, ex);
					throw new CMException(exMessage, ex);
				} finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			} else {
				// Otherwise create a new empty Keystore
				FileOutputStream fos = null;
				try {
					keystore.load(null, null);
					// Immediately save the new (empty) Keystore to the file
					fos = new FileOutputStream(keystoreFile);
					keystore.store(fos, masterPassword.toCharArray());
				} catch (Exception ex) {
					String exMessage = "Failed to generate a new empty Keystore.";
					// logger.error(exMessage, ex);
					throw new CMException(exMessage, ex);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}

			/*
			 * Taverna distro for MAC contains info.plist file with some Java
			 * system properties set to use the Keychain which clashes with what
			 * we are setting here so we need to clear them
			 */
			System.clearProperty(CredentialManager.PROPERTY_KEYSTORE_TYPE); // "javax.net.ssl.keyStoreType"
			System.clearProperty(CredentialManager.PROPERTY_KEYSTORE_PROVIDER); // "javax.net.ssl.keyStoreProvider"

			/*
			 * Not quite sure why we still need to set these two properties
			 * since we are creating our own SSLSocketFactory with our own
			 * KeyManager that uses Taverna's Keystore, but seem like after
			 * Taverna starts up and the first time it needs SSLSocketFactory
			 * for HTTPS connection it is still using the default Java's
			 * keystore unless these properties are set. Set the system property
			 * "javax.net.ssl.keystore" to use Taverna's keystore.
			 */
			// Axis 1 likes reading from these properties but seems to work as
			// well with
			// Taverna's SSLSocetFactory as well. We do not want to expose these
			// as they can be read from Beanshells.
			// System.setProperty(PROPERTY_KEYSTORE,
			// keystoreFile.getAbsolutePath());
			// System.setProperty(PROPERTY_KEYSTORE_PASSWORD, masterPassword);
			System.clearProperty(CredentialManager.PROPERTY_KEYSTORE); // "javax.net.ssl.keyStore"
			System.clearProperty(CredentialManager.PROPERTY_KEYSTORE_PASSWORD); // "javax.net.ssl.keyStorePassword"
		}
	}

	/**
	 * Load Taverna's Truststore from a file on a disk. If the Truststore does
	 * not already exist, a new empty one will be created and contents of Java's
	 * truststore located in <JAVA_HOME>/lib/security/cacerts will be copied
	 * over to the Truststore.
	 */
	private void loadTruststore() throws CMException {

		if (truststore == null) {
			try {
				// Try to create Taverna's Truststore as Bouncy Castle UBER-type
				// keystore.
				truststore = KeyStore.getInstance("UBER", "BC");
			} catch (Exception ex) {
				// The requested keystore type is not available from security
				// providers.
				String exMessage = "Failed to instantiate Taverna's Truststore.";
				throw new CMException(exMessage, ex);
			}

			if (truststoreFile.exists()) {
				// If the Truststore file already exists, open it and load the
				// Truststore
				FileInputStream fis = null;
				try {
					// Get the file
					fis = new FileInputStream(truststoreFile);
					// Load the Truststore from the file
					truststore.load(fis, masterPassword.toCharArray());
				} catch (Exception ex) {
					truststore = null;// make it null as it was just created but
										// failed to load so it is not null
					masterPassword = null; // it is probably the wrong password
											// so do not remember it just in
											// case
					String exMessage = "Failed to load Taverna's Truststore. Possible reason: incorrect password or corrupted file.";
					// logger.error(exMessage, ex);
					throw new CMException(exMessage, ex);
				} finally {
					if (fis != null) {
						try {
							fis.close();
							fis = null;
						} catch (IOException e) {
							// ignore
						}
					}
				}
			} else {
				/*
				 * Otherwise create a new empty Truststore and load it with
				 * certs from Java's truststore.
				 */
				File javaTruststoreFile = new File(
						System.getProperty("java.home")
								+ "/lib/security/cacerts");
				KeyStore javaTruststore = null;

				// Java's truststore is of type "JKS" - try to load it
				try {
					javaTruststore = KeyStore.getInstance("JKS");
				} catch (Exception ex) {
					// The requested keystore type is not available from the
					// provider
					String exMessage = "Failed to instantiate a 'JKS'-type keystore for reading Java's truststore.";
					// logger.error(exMessage, ex);
					throw new CMException(exMessage, ex);
				}

				FileInputStream fis = null;
				boolean loadedJavaTruststore = false;
				/*
				 * Load Java's truststore from the file - try with the default
				 * Java truststore passwords.
				 */
				for (String password : defaultTrustStorePasswords) {
					logger.info("Trying to load Java truststore using password: "
							+ password);
					try {
						// Get the file
						fis = new FileInputStream(javaTruststoreFile);
						javaTruststore.load(fis, password.toCharArray());
						loadedJavaTruststore = true;
						break;
					} catch (IOException ioex) {
						/*
						 * If there is an I/O or format problem with the
						 * keystore data, or if the given password was incorrect
						 * (Thank you Sun, now I can't know if it is the file or
						 * the password..)
						 */
						String message = "Failed to load the Java "
								+ "truststore to copy "
								+ "over certificates using default password: "
								+ password + " from " + javaTruststoreFile;
						logger.info(message);
					} catch (NoSuchAlgorithmException e) {
						logger.error("Unknown encryption algorithm "
								+ "while loading Java truststore from "
								+ javaTruststoreFile, e);
						break;
					} catch (CertificateException e) {
						logger.error("Certificate error while "
								+ "loading Java truststore from "
								+ javaTruststoreFile, e);
						break;
					} finally {
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
								logger.warn("Could not close input stream to "
										+ javaTruststoreFile, e);
							}
						}
					}
				}

				// Default Java truststore passwords failed - possibly the user
				// has changed it.
				// Ask the Java truststore password providers if they can help -
				// this will typically
				// pop up a dialog to ask the user if we are in a graphical
				// environment. If not, we
				// will simply not copy the default truststore certificates into
				// Credential
				// Manager's Truststore.
				if (!loadedJavaTruststore) {
					if (!(loadJavaTruststoreUsingPasswordProviders(
							javaTruststore, javaTruststoreFile))) {
						String error = "Credential manager failed to load"
								+ " certificates from Java's truststore.";
						String help = "Try using the system property -D"
								+ CredentialManager.PROPERTY_TRUSTSTORE_PASSWORD
								+ "=TheTrustStorePassword";
						logger.error(error + " " + help);
						System.err.println(error);
						System.err.println(help);
					}
				}

				FileOutputStream fos = null;
				// Create a new empty Truststore for Taverna
				try {
					truststore.load(null, null);
					if (loadedJavaTruststore) {
						// Copy certificates into Taverna's Truststore from
						// Java's truststore.
						Enumeration<String> aliases = javaTruststore.aliases();
						while (aliases.hasMoreElements()) {
							String alias = aliases.nextElement();
							Certificate certificate = javaTruststore
									.getCertificate(alias);
							if (certificate instanceof X509Certificate) {
								String trustedCertAlias = createTrustedCertificateAlias((X509Certificate) certificate);
								truststore.setCertificateEntry(
										trustedCertAlias, certificate);
							}
						}
					}
					// Immediately save the new Truststore to the file
					fos = new FileOutputStream(truststoreFile);
					truststore.store(fos, masterPassword.toCharArray());
				} catch (Exception ex) {
					truststore = null;// make it null as it was just created but
										// failed to save so we should retry
										// next time
					String exMessage = "Failed to generate new empty Taverna's Truststore.";
					// logger.error(exMessage, ex);
					throw new CMException(exMessage, ex);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}

			/*
			 * Taverna distro for MAC contains info.plist file with some Java
			 * system properties set to use the Keychain which clashes with what
			 * we are setting here so we need to clear them.
			 */
			System.clearProperty(CredentialManager.PROPERTY_TRUSTSTORE_TYPE); // "javax.net.ssl.trustStoreType"
			System.clearProperty(CredentialManager.PROPERTY_TRUSTSTORE_PROVIDER);// "javax.net.ssl.trustStoreProvider"

			/*
			 * Not quite sure why we still need to set these two properties
			 * since we are creating our own SSLSocketFactory with our own
			 * TrustManager that uses Taverna's Truststore, but seem like after
			 * Taverna starts up and the first time it needs SSLSocketFactory
			 * for HTTPS connection it is still using the default Java's
			 * truststore unless these properties are set. Set the system
			 * property "javax.net.ssl.Truststore" to use Taverna's truststore.
			 */

			// Axis 1 likes reading from these properties but seems to work as
			// well with
			// Taverna's SSLSocetFactory as well. We do not want to expose these
			// as they can be read from Beanshells.
			// System.setProperty(PROPERTY_TRUSTSTORE, truststoreFile
			// .getAbsolutePath());
			// System.setProperty(PROPERTY_TRUSTSTORE_PASSWORD, masterPassword);
			System.clearProperty(CredentialManager.PROPERTY_TRUSTSTORE); // "javax.net.ssl.trustStore"
			System.clearProperty(CredentialManager.PROPERTY_TRUSTSTORE_PASSWORD); // "javax.net.ssl.trustStorePassword"

		}
	}

	/**
	 * Load the given keystore (which is Java's default truststore) from the
	 * given file (pointing to the Java's default truststore) using the
	 * {@link JavaTruststorePasswordProvider}s lookup to obtain the password for
	 * the keytore.
	 * 
	 * @param javaTruststore
	 *            Java's default truststore
	 * @param javaTruststoreFile
	 *            Java's default truststore file
	 * @return true if managed to load the keystore using the provided
	 *         passwords; false otherwise
	 */
	private boolean loadJavaTruststoreUsingPasswordProviders(
			KeyStore javaTruststore, File javaTruststoreFile) {
		String javaTruststorePassword = null;
		for (JavaTruststorePasswordProvider provider : javaTruststorePasswordProviders) {
			javaTruststorePassword = provider.getJavaTruststorePassword();
			if (javaTruststorePassword == null) {
				continue;
			}
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(javaTruststoreFile);
				javaTruststore.load(fis, javaTruststorePassword.toCharArray());
				return true;
			} catch (Exception ex) {
				String exMessage = "Failed to load the Java truststore to copy over certificates"
						+ " using user-provided password from password provider "
						+ provider;
				logger.warn(exMessage, ex);
				return false;
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
		String exMessage = "None (if any) MasterPasswordProviderSPI could unlock "
				+ "Java's truststore. Creating a new empty "
				+ "Truststore for Taverna.";
		logger.error(exMessage);
		return false;
	}

	/**
	 * Get a username and password pair for the given service, or null if it
	 * does not exit. The returned array contains username as the first element
	 * and password as the second.
	 * 
	 * @deprecated Use
	 *             {@link #getUsernameAndPasswordForService(URI, boolean, String)}
	 *             instead
	 */
	@Deprecated
	public String[] getUsernameAndPasswordForService(String serviceURL)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		UsernamePassword usernamePassword = getUsernameAndPasswordForService(
				URI.create(serviceURL), false, null);
		if (usernamePassword == null) {
			return null;
		}
		String[] pair = new String[2];
		pair[0] = usernamePassword.getUsername();
		pair[1] = String.valueOf(usernamePassword.getPassword());
		usernamePassword.resetPassword();
		return pair;
	}

	/**
	 * Get a username and password pair for the given service's URI, or null if
	 * it does not exit.
	 * <p>
	 * If the username and password are not available in the Keystore, it will
	 * invoke implementations of the {@link ServiceUsernameAndPasswordProvider}
	 * interface asking the user (typically through the UI) or resolving
	 * hard-coded credentials.
	 * <p>
	 * If the parameter <code>useURIPathRecursion</code> is true, then the
	 * Credential Manager will also attempt to look for stored credentials for
	 * each of the parent fragments of the URI.
	 * 
	 * @param serviceURI
	 *            The URI of the service for which we are providing the username
	 *            and password
	 * 
	 * @param useURIPathRecursion
	 *            Whether to look for any username and passwords stored in the
	 *            Keystore for the parent fragments of the service URI (for
	 *            example, we are looking for the credentials for service
	 *            http://somehost/some-fragment but we already have credentials
	 *            stored for http://somehost which can be reused)
	 * 
	 * @param requestingMessage
	 *            The message to be presented to the user when asking for the
	 *            username and password, normally useful for UI providers that
	 *            pop up dialogs, can be ignored otherwise
	 * 
	 * @return username and password pair for the given service
	 * 
	 * @throws CMException
	 *             if anything goes wrong during Keystore lookup, etc.
	 */
	@Override
	public UsernamePassword getUsernameAndPasswordForService(URI serviceURI,
			boolean usePathRecursion, String requestingMessage)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (keystore) {

			SecretKeySpec passwordKey = null;
			LinkedHashSet<URI> possibleServiceURIsToLookup = getPossibleServiceURIsToLookup(
					serviceURI, usePathRecursion);

			Map<URI, URI> allServiceURIs = getFragmentMappedURIsForAllUsernameAndPasswordPairs();

			try {
				for (URI lookupURI : possibleServiceURIsToLookup) {
					URI mappedURI = allServiceURIs.get(lookupURI);
					if (mappedURI == null) {
						continue;
					}
					// We found it - get the username and password in the
					// Keystore associated with this service URI
					String alias = null;
					alias = "password#" + mappedURI.toASCIIString();
					passwordKey = (((SecretKeySpec) keystore.getKey(alias,
							null)));
					if (passwordKey == null) {
						// Unexpected, it was just there in the map!
						logger.warn("Could not find alias " + alias
								+ " for known uri " + lookupURI
								+ ", just deleted?");
						// Remember we went outside synchronized(keystore) while
						// looping
						continue;
					}
					String unpasspair = new String(passwordKey.getEncoded(),
							CredentialManager.UTF_8);
					/*
					 * decoded key contains string
					 * <USERNAME><SEPARATOR_CHARACTER><PASSWORD>
					 */

					int separatorAt = unpasspair
							.indexOf(CredentialManager.USERNAME_AND_PASSWORD_SEPARATOR_CHARACTER);
					if (separatorAt < 0) {
						throw new CMException("Invalid credentials stored for "
								+ lookupURI);
					}
					String username = unpasspair.substring(0, separatorAt);
					String password = unpasspair.substring(separatorAt + 1);

					UsernamePassword usernamePassword = new UsernamePassword();
					usernamePassword.setUsername(username);
					usernamePassword.setPassword(password.toCharArray());
					return usernamePassword;
				}

				// Nothing found in the Keystore, let's lookup using the service
				// username and password providers
				for (ServiceUsernameAndPasswordProvider serviceUsernameAndPasswordProvider : serviceUsernameAndPasswordProviders) {
					UsernamePassword usernamePassword = serviceUsernameAndPasswordProvider
							.getServiceUsernameAndPassword(serviceURI,
									requestingMessage);
					if (usernamePassword == null) {
						continue;
					}
					if (usernamePassword.isShouldSave()) {
						URI uri = serviceURI;
						if (usePathRecursion) {
							uri = normalizeServiceURI(serviceURI);
						}
						addUsernameAndPasswordForService(usernamePassword, uri);
					}
					return usernamePassword;
				}
				// Giving up
				return null;
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to get the username and password pair for service "
						+ serviceURI + " from the Keystore.";
				logger.error(exMessage, ex);
				throw new CMException(exMessage, ex);
			}
		}
	}

	protected Map<URI, URI> getFragmentMappedURIsForAllUsernameAndPasswordPairs()
			throws CMException {

		synchronized (Security.class) {
			if (cachedServiceURIsMap == null) {
				HashMap<URI, URI> map = new HashMap<URI, URI>();
				// Get all service URIs that have username and password in the
				// Keystore
				List<URI> serviceURIs = getServiceURIsForAllUsernameAndPasswordPairs();
				for (URI serviceURI : serviceURIs) {
					// Always store 1-1, with or without fragment
					map.put(serviceURI, serviceURI);
					if (serviceURI.getFragment() == null) {
						continue;
					}
					// Look up the no-fragment uri as an additional mapping
					URI noFragment;
					try {
						noFragment = setFragmentForURI(serviceURI, null);
					} catch (URISyntaxException e) {
						logger.warn("Could not reset fragment for service URI "
								+ serviceURI);
						continue;
					}
					if (map.containsKey(noFragment)) {
						if (map.get(noFragment).getFragment() != null) {
							// No mapping for duplicates
							map.remove(noFragment);
							continue;
						} // else it's noFragment -> noFragment, which is OK
					} else {
						// Brand new, put it in
						map.put(noFragment, serviceURI);
					}
				}
				cachedServiceURIsMap = map;
			}
			return cachedServiceURIsMap;
		}
	}

	/*
	 * Creates a list of possible URIs to look up when searching for username
	 * and password for a service with a given URI. This is mainly useful for
	 * HTTP AuthN when we save the realm URI rather than the exact service URI
	 * as we want that username and password pair to be used for the whole realm
	 * and not bother user for credentials every time them access a URL from
	 * that realm.
	 */
	protected LinkedHashSet<URI> getPossibleServiceURIsToLookup(
			URI serviceURI, boolean usePathRecursion) {

		try {
			serviceURI = serviceURI.normalize();
			serviceURI = setUserInfoForURI(serviceURI, null);
		} catch (URISyntaxException ex) {
			logger.warn("Could not strip userinfo from " + serviceURI, ex);
		}

		/*
		 * We'll use a LinkedHashSet to avoid checking for duplicates, like if
		 * serviceURI.equals(withoutQuery) Only the first hit should be added to
		 * the set.
		 */
		LinkedHashSet<URI> possibles = new LinkedHashSet<URI>();

		possibles.add(serviceURI);
		if (!usePathRecursion || !serviceURI.isAbsolute()) {
			return possibles;
		}

		/*
		 * We'll preserve the fragment, as it is used to indicate the realm
		 */
		String rawFragment = serviceURI.getRawFragment();
		if (rawFragment == null) {
			rawFragment = "";
		}
		URI withoutQuery = serviceURI.resolve(serviceURI.getRawPath());
		addFragmentedURI(possibles, withoutQuery, rawFragment);

		// Immediate parent
		URI parent = withoutQuery.resolve(".");
		addFragmentedURI(possibles, parent, rawFragment);
		URI oldParent = null;
		// Top parent (to be added later)
		URI root = parent.resolve("/");
		while (!parent.equals(oldParent) && !parent.equals(root)
				&& parent.getPath().length() > 0) {
			// Intermediate parents, but not for "http://bla.org" as we would
			// find "http://bla.org.."
			oldParent = parent;
			parent = parent.resolve("..");
			addFragmentedURI(possibles, parent, rawFragment);
		}
		// In case while-loop did not do so, also include root
		addFragmentedURI(possibles, root, rawFragment);
		if (rawFragment.length() > 0) {
			// Add the non-fragment versions in the bottom of the list
			for (URI withFragment : new ArrayList<URI>(possibles)) {
				try {
					possibles.add(setFragmentForURI(withFragment, null));
				} catch (URISyntaxException e) {
					logger.warn("Could not non-fragment URI " + withFragment);
				}
			}
		}
		return possibles;
	}

	public void addFragmentedURI(LinkedHashSet<URI> possibles, URI uri,
			String rawFragment) {
		if (rawFragment != null && rawFragment.length() > 0) {
			uri = uri.resolve("#" + rawFragment);
		}
		possibles.add(uri);
	}

	/**
	 * Get service URLs associated with all username/password pairs currently in
	 * the Keystore.
	 * 
	 * @deprecated
	 * @see #getServiceURIsForAllUsernameAndPasswordPairs()
	 */
	@Deprecated
	public ArrayList<String> getServiceURLsforAllUsernameAndPasswordPairs()
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		List<URI> uris = getServiceURIsForAllUsernameAndPasswordPairs();
		ArrayList<String> serviceURLs = new ArrayList<String>();
		for (URI uri : uris) {
			serviceURLs.add(uri.toASCIIString());
		}
		return serviceURLs;
	}

	/**
	 * Insert a username and password pair for the given service URI in the
	 * Keystore.
	 * <p>
	 * Effectively, this method inserts a new secret key entry in the Keystore,
	 * where key contains <USERNAME>"\000"<PASSWORD> string, i.e. password is
	 * prepended with the username and separated by a \000 character (which
	 * hopefully will not appear in the username).
	 * <p>
	 * Username and password string is saved in the Keystore as byte array using
	 * SecretKeySpec (which constructs a secret key from the given byte array
	 * but does not check if the given bytes indeed specify a secret key of the
	 * specified algorithm).
	 * <p>
	 * An alias used to identify the username and password entry is constructed
	 * as "password#"<SERVICE_URL> using the service URL this username/password
	 * pair is to be used for.
	 * <p>
	 * 
	 * @param usernamePassword
	 *            The {@link UsernamePassword} to store
	 * @param serviceURI
	 *            The (possibly normalized) URI to store the credentials under
	 * @throws CMException
	 *             If the credentials could not be stored
	 * @return the alias under which this username and password entry was saved in the Keystore
	 */
	@Override
	public String addUsernameAndPasswordForService(
			UsernamePassword usernamePassword, URI serviceURI)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		String uriString = serviceURI.toASCIIString();
		String alias = saveUsernameAndPasswordForService(usernamePassword.getUsername(),
				String.valueOf(usernamePassword.getPassword()), uriString);
		return alias;
	}

	/**
	 * Insert a new username and password pair in the Keystore for the given
	 * service URL string.
	 * <p>
	 * Effectively, this method inserts a new secret key entry in the Keystore,
	 * where key contains <USERNAME>"\000"<PASSWORD> string, i.e. password is
	 * prepended with the username and separated by a \000 character.
	 * <p>
	 * Username and password string is saved in the Keystore as byte array using
	 * SecretKeySpec (which constructs a secret key from the given byte array
	 * but does not check if the given bytes indeed specify a secret key of the
	 * specified algorithm).
	 * <p>
	 * An alias used to identify the username and password entry is constructed
	 * as "password#"<SERVICE_URL> using the service URL this username/password
	 * pair is to be used for.
	 * <p>
	 * 
	 * @return the alias under which this username and password entry was saved in the Keystore

	 * @deprecated Use
	 *             {@link #addUsernameAndPasswordForService(UsernamePassword, URI)}
	 *             instead
	 */
	@Deprecated
	public String saveUsernameAndPasswordForService(String username,
			String password, String serviceURL) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		String alias = null;
		
		// Alias for the username and password entry
		synchronized (keystore) {

			alias = "password#" + serviceURL;
			/*
			 * Password (together with its related username) is wrapped as a
			 * SecretKeySpec that implements SecretKey and constructs a secret
			 * key from the given password as a byte array. The reason for this
			 * is that we can only save instances of Key objects in the
			 * Keystore, and SecretKeySpec class is useful for raw secret keys
			 * (i.e. username and passwords concats) that can be represented as
			 * a byte array and have no key or algorithm parameters associated
			 * with them, e.g., DES or Triple DES. That is why we create it with
			 * the name "DUMMY" for algorithm name, as this is not checked for
			 * anyway.
			 * 
			 * Use a separator character that will not appear in the username or
			 * password.
			 */
			String keyToSave = username
					+ CredentialManager.USERNAME_AND_PASSWORD_SEPARATOR_CHARACTER
					+ password;

			SecretKeySpec passwordKey;
			try {
				passwordKey = new SecretKeySpec(
						keyToSave.getBytes(CredentialManager.UTF_8),
						"DUMMY");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Could not find encoding "
						+ CredentialManager.UTF_8);
			}
			try {
				keystore.setKeyEntry(alias, passwordKey,
						null, null);
				saveKeystore(KeystoreType.KEYSTORE);
				multiCaster.notify(new KeystoreChangedEvent(
						KeystoreType.KEYSTORE));
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to insert username and password pair for service "
						+ serviceURL + " in the Keystore.";
				logger.error(exMessage, ex);
				throw new CMException(exMessage, ex);
			}
		}
		
		return alias;
	}

	/**
	 * Delete a username and password pair for the given service URI from the
	 * Keystore.
	 */
	@Override
	public void deleteUsernameAndPasswordForService(URI serviceURI)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		String uriString = serviceURI.toASCIIString();
		deleteUsernameAndPasswordForService(uriString);
	}

	/**
	 * Delete a username and password pair for the given service URL string from
	 * the Keystore.
	 * 
	 * @deprecated Use
	 *             {@link #deleteUsernameAndPasswordForService(URI serviceURI)}
	 *             instead.
	 */
	@Deprecated
	public void deleteUsernameAndPasswordForService(String serviceURL)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (keystore) {
			deleteEntry(KeystoreType.KEYSTORE, "password#" + serviceURL);
			saveKeystore(KeystoreType.KEYSTORE);
			multiCaster.notify(new KeystoreChangedEvent(KeystoreType.KEYSTORE));
		}
	}

	/**
	 * Insert a new key entry containing private key and the corresponding
	 * public key certificate chain in the Keystore.
	 * 
	 * An alias used to identify the keypair entry is constructed as:
	 * "keypair#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<
	 * CERT_SERIAL_NUMBER>
	 * 
	 * @return the alias under which this key entry was saved in the Keystore
	 */
	@Override
	public String addKeyPair(Key privateKey, Certificate[] certs)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		String alias = null;

		synchronized (keystore) {
			// Create an alias for the new key pair entry in the Keystore
			// as
			// "keypair#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<CERT_SERIAL_NUMBER>
			String ownerDN = ((X509Certificate) certs[0])
					.getSubjectX500Principal().getName(X500Principal.RFC2253);
			CMUtils util = new CMUtils();
			util.parseDN(ownerDN);
			String ownerCN = util.getCN(); // owner's common name

			// Get the hexadecimal representation of the certificate's serial
			// number
			String serialNumber = new BigInteger(1,
					((X509Certificate) certs[0]).getSerialNumber()
							.toByteArray()).toString(16).toUpperCase();

			String issuerDN = ((X509Certificate) certs[0])
					.getIssuerX500Principal().getName(X500Principal.RFC2253);
			util.parseDN(issuerDN);
			String issuerCN = util.getCN(); // issuer's common name

			alias = "keypair#" + ownerCN + "#" + issuerCN + "#"
					+ serialNumber;

			try {
				keystore.setKeyEntry(alias, privateKey,
						null, certs);
				saveKeystore(KeystoreType.KEYSTORE);
				multiCaster.notify(new KeystoreChangedEvent(
						KeystoreType.KEYSTORE));

				// This is now done from the KeystoresChangedObserver's notify
				// method.
				// Update the default SSLSocketFactory used by the
				// HttpsURLConnectionS
				// HttpsURLConnection.setDefaultSSLSocketFactory(createTavernaSSLSocketFactory());
				logger.info("Credential Manager: Updating SSLSocketFactory after inserting a key pair.");
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to insert the key pair entry in the Keystore.";
				// logger.error(exMessage, ex);
				throw (new CMException(exMessage, ex));
			}	
		}
		return alias;
	}

	/**
	 * Checks if the Keystore contains the given key pair entry (private key and
	 * its corresponding public key certificate chain).
	 */
	@Override
	public boolean hasKeyPair(Key privateKey, Certificate[] certs)
			throws CMException {

		// Create an alias for the new key pair entry in the Keystore as
		// "keypair#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<CERT_SERIAL_NUMBER>

		String alias = createKeyPairAlias(privateKey, certs);
		return hasEntryWithAlias(KeystoreType.KEYSTORE, alias);

	}

	/**
	 * Delete a key pair entry from the Keystore given its alias.
	 */
	@Override
	public void deleteKeyPair(String alias) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (keystore) {
			deleteEntry(KeystoreType.KEYSTORE, alias);
			saveKeystore(KeystoreType.KEYSTORE);
			multiCaster.notify(new KeystoreChangedEvent(KeystoreType.KEYSTORE));

			// This is now done from the KeyManager's nad TrustManager's notify
			// methods
			// Update the default SSLSocketFactory used by the
			// HttpsURLConnectionS
			// HttpsURLConnection.setDefaultSSLSocketFactory(createTavernaSSLSocketFactory());

			logger.info("Credential Manager: Updating SSLSocketFactory "
					+ "after deleting a keypair.");
		}
	}
	
	/**
	 * Delete a key pair entry from the Keystore given its private and public key parts.
	 */
	@Override
	public void deleteKeyPair(Key privateKey, Certificate[] certs)
			throws CMException {
		
		String alias = createKeyPairAlias(privateKey, certs);
		deleteKeyPair(alias);
	}	

	/**
	 * Export a key entry containing private key and public key certificate
	 * chain from the Keystore to a PKCS #12 file.
	 */
	@Override
	public void exportKeyPair(String alias, File exportFile,
			String pkcs12Password) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		FileOutputStream fos = null;

		synchronized (keystore) {
			// Export the key pair
			try {

				// Get the private key for the alias
				PrivateKey privateKey = (PrivateKey) keystore.getKey(alias,
						null);

				// Get the related public key's certificate chain
				Certificate[] certChain = getKeyPairsCertificateChain(alias);

				// Create a new PKCS #12 keystore
				KeyStore newPkcs12 = KeyStore.getInstance("PKCS12", "BC");
				newPkcs12.load(null, null);

				// Place the private key and certificate chain into the PKCS #12
				// keystore.
				// Construct a new alias as
				// "<SUBJECT_COMMON_NAME>'s <ISSUER_ORGANISATION> ID"

				String sDN = ((X509Certificate) certChain[0])
						.getSubjectX500Principal().getName(
								X500Principal.RFC2253);
				CMUtils util = new CMUtils();
				util.parseDN(sDN);
				String sCN = util.getCN();

				String iDN = ((X509Certificate) certChain[0])
						.getIssuerX500Principal()
						.getName(X500Principal.RFC2253);
				util.parseDN(iDN);
				String iCN = util.getCN();

				String pkcs12Alias = sCN + "'s " + iCN + " ID";
				newPkcs12.setKeyEntry(pkcs12Alias, privateKey, new char[0],
						certChain);

				// Store the new PKCS #12 keystore on the disk
				fos = new FileOutputStream(exportFile);
				newPkcs12.store(fos, pkcs12Password.toCharArray());
				fos.close();
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to export the key pair from the Keystore.";
				logger.error(exMessage, ex);
				throw new CMException(exMessage, ex);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}

	/**
	 * Get certificate entry from the Keystore or Truststore. If the given alias
	 * name identifies a trusted certificate entry, the certificate associated
	 * with that entry is returned from the Truststore. If the given alias name
	 * identifies a key pair entry, the first element of the certificate chain
	 * of that entry is returned from the Keystore.
	 */
	@Override
	public Certificate getCertificate(KeystoreType ksType, String alias)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		try {
			if (ksType.equals(KeystoreType.KEYSTORE)) {
				synchronized (keystore) {
					return keystore.getCertificate(alias);
				}
			} else if (ksType.equals(KeystoreType.TRUSTSTORE)) {
				synchronized (truststore) {
					return truststore.getCertificate(alias);
				}
			} else {
				return null;
			}
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to fetch certificate from the "
					+ ksType + ".";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		}
	}

	/**
	 * Get certificate chain for the key pair entry from the Keystore. This
	 * method works for the Keystore only as the Truststore does not contain key
	 * pair entries, but trusted certificate entries only.
	 */
	@Override
	public Certificate[] getKeyPairsCertificateChain(String alias)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (keystore) {
			try {
				return keystore.getCertificateChain(alias);
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to fetch certificate chain for the keypair from the Keystore";
				logger.error(exMessage, ex);
				throw new CMException(exMessage, ex);
			}
		}
	}

	/**
	 * Get the private key part of a key pair entry from the Keystore given its alias. 
	 * <p>
	 * This method works for the Keystore only as the Truststore does not contain key pair
	 * entries, but trusted certificate entries only.
	 * @throws CMException 
	 */
	public Key getKeyPairsPrivateKey(String alias) throws CMException{
		
		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (keystore) {
			try {
				return keystore.getKey(alias, null);
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to fetch private key for the keypair from the Keystore";
				logger.error(exMessage, ex);
				throw new CMException(exMessage, ex);
			}
		}
	}
	
	/**
	 * Insert a trusted certificate entry in the Truststore with an alias
	 * constructed as:
	 * 
	 * "trustedcert#<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#
	 * "<CERT_SERIAL_NUMBER>
	 *
	 * @return the alias under which this trusted certificate entry was saved in the Keystore
	 */
	@Override
	public String addTrustedCertificate(X509Certificate cert) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();
		
		String alias = null;
		
		synchronized (truststore) {
			// Create an alias for the new trusted certificate entry in the Truststore as
			// "trustedcert#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<CERT_SERIAL_NUMBER>
			alias = createTrustedCertificateAlias(cert);
			try {
				truststore.setCertificateEntry(alias, cert);
				saveKeystore(KeystoreType.TRUSTSTORE);
				multiCaster.notify(new KeystoreChangedEvent(
						KeystoreType.TRUSTSTORE));

				// This is now done from the KeystoresChangedObserver's notify
				// method.
				// Update the default SSLSocketFactory used by the
				// HttpsURLConnectionS
				// HttpsURLConnection.setDefaultSSLSocketFactory(createTavernaSSLSocketFactory());

				logger.info("Credential Manager: Updating SSLSocketFactory after inserting a trusted certificate.");
			} catch (Exception ex) {
				String exMessage = "Credential Manager: Failed to insert trusted certificate entry in the Truststore.";
				// logger.error(exMessage, ex);
				throw (new CMException(exMessage, ex));
			}
		}
		
		return alias;
	}

	/**
	 * Create a Keystore alias that would be used for adding the given 
	 * key pair (private and public key) entry to the Keystore. The alias is cretaed as 
	 * "keypair#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<CERT_SERIAL_NUMBER>
	 * 
	 * @param privateKey private key
	 * @param certs public key's certificate chain
	 * @return
	 */
	public String createKeyPairAlias(Key privateKey, Certificate certs[]){

		String ownerDN = ((X509Certificate) certs[0])
				.getSubjectX500Principal().getName(X500Principal.RFC2253);
		CMUtils util = new CMUtils();
		util.parseDN(ownerDN);
		String ownerCN = util.getCN(); // owner's common name

		// Get the hexadecimal representation of the certificate's serial
		// number
		String serialNumber = new BigInteger(1,
				((X509Certificate) certs[0]).getSerialNumber()
						.toByteArray()).toString(16).toUpperCase();

		String issuerDN = ((X509Certificate) certs[0])
				.getIssuerX500Principal().getName(X500Principal.RFC2253);
		util.parseDN(issuerDN);
		String issuerCN = util.getCN(); // issuer's common name

		String alias = "keypair#" + ownerCN + "#" + issuerCN + "#"
				+ serialNumber;	
		return alias;
	}
	
	/**
	 * Create a Truststore alias that would be used for adding the given 
	 * trusted X509 certificate to the Truststore. The alias is cretaed as 
	 * "trustedcert#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<
	 * CERT_SERIAL_NUMBER>
	 * 
	 * @param cert certificate to generate the alias for
	 * @return the alias for the given certificate
	 */
	public String createTrustedCertificateAlias(X509Certificate cert) {
		String ownerDN = cert.getSubjectX500Principal().getName(
				X500Principal.RFC2253);
		CMUtils util = new CMUtils();
		util.parseDN(ownerDN);
		String owner;
		String ownerCN = util.getCN(); // owner's common name
		String ownerOU = util.getOU();
		String ownerO = util.getO();
		if (!ownerCN.equals("none")) { // try owner's CN first
			owner = ownerCN;
		} // try owner's OU
		else if (!ownerOU.equals("none")) {
			owner = ownerOU;
		} else if (!ownerO.equals("none")) { // finally use owner's Organisation
			owner = ownerO;
		} else {
			owner = "<Not Part of Certificate>";
		}

		// Get the hexadecimal representation of the certificate's serial number
		String serialNumber = new BigInteger(1, cert.getSerialNumber()
				.toByteArray()).toString(16).toUpperCase();

		String issuerDN = cert.getIssuerX500Principal().getName(
				X500Principal.RFC2253);
		util.parseDN(issuerDN);
		String issuer;
		String issuerCN = util.getCN(); // issuer's common name
		String issuerOU = util.getOU();
		String issuerO = util.getO();
		if (!issuerCN.equals("none")) { // try issuer's CN first
			issuer = issuerCN;
		} // try issuer's OU
		else if (!issuerOU.equals("none")) {
			issuer = issuerOU;
		} else if (!issuerO.equals("none")) { // finally use issuer's
			// Organisation
			issuer = issuerO;
		} else {
			issuer = "<Not Part of Certificate>";
		}

		String alias = "trustedcert#" + owner + "#" + issuer + "#"
				+ serialNumber;
		return alias;
	}

	/**
	 * Checks if the Truststore contains the given public key certificate.
	 */
	public boolean hasTrustedCertificate(Certificate cert)
			throws CMException{

		// Create an alias for the new trusted certificate entry in the Truststore as
		// "trustedcert#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<CERT_SERIAL_NUMBER>
		String alias = createTrustedCertificateAlias((X509Certificate) cert);
		return hasEntryWithAlias(KeystoreType.TRUSTSTORE, alias);

	}
	
	/**
	 * Delete a trusted certificate entry from the Truststore given its alias.
	 */
	@Override
	public void deleteTrustedCertificate(String alias) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (truststore) {
			deleteEntry(KeystoreType.TRUSTSTORE, alias);
			saveKeystore(KeystoreType.TRUSTSTORE);
			multiCaster
					.notify(new KeystoreChangedEvent(KeystoreType.TRUSTSTORE));

			// This is now done from the KeyManager's nad TrustManager's notify
			// methods
			// Update the default SSLSocketFactory used by the
			// HttpsURLConnectionS
			// HttpsURLConnection.setDefaultSSLSocketFactory(createTavernaSSLSocketFactory());

			logger.info("Credential Manager: Updating SSLSocketFactory "
					+ "after deleting a trusted certificate.");
		}
	}

	/**
	 * Delete a trusted certificate entry from the Truststore given the certificate.
	 */
	public void deleteTrustedCertificate(X509Certificate cert)
			throws CMException{
		String alias = createTrustedCertificateAlias(cert);
		deleteTrustedCertificate(alias);
	}
	
	/**
	 * Check if the given alias identifies is a key entry in the Keystore.
	 */
	@Override
	public boolean isKeyEntry(String alias) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		try {
			synchronized (keystore) {
				return keystore.isKeyEntry(alias);
			}
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to access the key entry in the Keystore.";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		}
	}

	/**
	 * Delete an entry from the Keystore or the Truststore.
	 */
	private void deleteEntry(KeystoreType ksType, String alias)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		try {
			if (ksType.equals(KeystoreType.KEYSTORE)) {
				synchronized (keystore) {
					if (keystore.containsAlias(alias)){
						keystore.deleteEntry(alias);						
					}
				}
			} else if (ksType.equals(KeystoreType.TRUSTSTORE)) {
				synchronized (truststore) {
					if (truststore.containsAlias(alias)){
						truststore.deleteEntry(alias);
					}
				}
			}
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to delete the entry with alias "
					+ alias + "from the " + ksType + ".";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		}
	}

	/**
	 * Check if the Keystore/Truststore contains an entry with the given alias.
	 */
	@Override
	public boolean hasEntryWithAlias(KeystoreType ksType, String alias)
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		try {
			if (ksType.equals(KeystoreType.KEYSTORE))
				synchronized (keystore) {
					return keystore.containsAlias(alias);
				}
			else if (ksType.equals(KeystoreType.TRUSTSTORE))
				synchronized (truststore) {
					return truststore.containsAlias(alias);
				}
			else {
				return false;
			}
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to access the "
					+ ksType + " to check if an alias exists.";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		}
	}

	/**
	 * Get all the aliases from the Keystore/Truststore or null if there was
	 * some error while accessing it.
	 */
	@Override
	public ArrayList<String> getAliases(KeystoreType ksType) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		try {
			if (ksType.equals(KeystoreType.KEYSTORE)) {
				synchronized (keystore) {
					return Collections.list(keystore.aliases());
				}
			} else if (ksType.equals(KeystoreType.TRUSTSTORE)) {
				synchronized (truststore) {
					return Collections.list(truststore.aliases());
				}
			} else {
				return null;
			}
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to access the "
					+ ksType + " to get the aliases.";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		}
	}

	/**
	 * Get service URIs associated with all username/password pairs currently in
	 * the Keystore.
	 * 
	 * @see #hasUsernamePasswordForService(URI)
	 */
	@Override
	public List<URI> getServiceURIsForAllUsernameAndPasswordPairs()
			throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		synchronized (keystore) {
			if (cachedServiceURIsList == null) {
				List<URI> serviceURIs = new ArrayList<URI>();
				for (String alias : getAliases(KeystoreType.KEYSTORE)) {
					/*
					 * We are only interested in username/password entries here.
					 * Alias for such entries is constructed as
					 * "password#"<SERVICE_URI> where SERVICE_URI is the service
					 * this username/password pair is to be used for.
					 */
					if (!alias.startsWith("password#")) {
						continue;
					}
					String[] split = alias.split("#", 2);
					if (split.length != 2) {
						logger.warn("Invalid alias " + alias);
						continue;
					}
					String uriStr = split[1];
					URI uri = URI.create(uriStr);
					serviceURIs.add(uri);
				}
				cachedServiceURIsList = serviceURIs;
			}
			return cachedServiceURIsList;
		}
	}

	/**
	 * Load a PKCS12-type keystore from a file using the supplied password.
	 */
	@Override
	public KeyStore loadPKCS12Keystore(File pkcs12File, String pkcs12Password)
			throws CMException {

		// Load the PKCS #12 keystore from the file
		KeyStore pkcs12;
		try {
			pkcs12 = KeyStore.getInstance("PKCS12", "BC");
			pkcs12.load(new FileInputStream(pkcs12File),
					pkcs12Password.toCharArray());
			return pkcs12;
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to open a PKCS12-type keystore.";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		}
	}

	/**
	 * Add an observer of the changes to the Keystore or Truststore.
	 */
	@Override
	public void addObserver(Observer<KeystoreChangedEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * Get all current observers of changes to the Keystore or Truststore.
	 */
	@Override
	public List<Observer<KeystoreChangedEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	/**
	 * Remove an observer of the changes to the Keystore or Truststore.
	 */
	@Override
	public void removeObserver(Observer<KeystoreChangedEvent> observer) {
		multiCaster.removeObserver(observer);
	}

//	/**
//	 * Checks if Credential Manager has been initialised.
//	 */
//	public boolean isInitialized() {
//		return isInitialized;
//	}

//	/**
//	 * Check if Keystore/Truststore file already exists on disk.
//	 */
//	private boolean exists(KeystoreType ksType) {
//
//		if (ksType.equals(KeystoreType.KEYSTORE))
//			return keystoreFile.exists();
//		else if (ksType.equals(KeystoreType.TRUSTSTORE)) {
//			return truststoreFile.exists();
//		} else
//			return false;
//	}

	/**
	 * Save the Keystore back to the file it was originally loaded from.
	 */
	private void saveKeystore(KeystoreType ksType) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		FileOutputStream fos = null;
		try {
			if (ksType.equals(KeystoreType.KEYSTORE)) {
				synchronized (keystore) {
					fos = new FileOutputStream(keystoreFile);
					keystore.store(fos, masterPassword.toCharArray());
				}

			} else if (ksType.equals(KeystoreType.TRUSTSTORE)) {
				synchronized (truststore) {
					fos = new FileOutputStream(truststoreFile);
					// Hard-coded trust store password
					truststore.store(fos, masterPassword.toCharArray());
				}
			}
		} catch (Exception ex) {
			String exMessage = "Credential Manager: Failed to save the "
					+ ksType + ".";
			logger.error(exMessage, ex);
			throw new CMException(exMessage, ex);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Checks if Keystore's master password is the same as the one provided.
	 */
	public boolean confirmMasterPassword(String password) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		return ((masterPassword != null) && masterPassword.equals(password));
	}

	/**
	 * Change the Keystore and the Truststore's master password to the one
	 * provided. The Keystore and Truststore both use the same password.
	 */
	public void changeMasterPassword(String newPassword) throws CMException {

		// Need to make sure we are initialized before we do anything else
		// as Credential Manager can be created but not initialized
		initialize();

		masterPassword = newPassword;
		saveKeystore(KeystoreType.KEYSTORE);
		saveKeystore(KeystoreType.TRUSTSTORE);
	}

	public void initializeSSL() throws CMException {
		if (tavernaSSLSocketFactory == null) {
			// We use the lazy initialization of Credential Manager from inside
			// the Taverna's SSLSocketFactory (i.e. KeyManager's and
			// TrustManager's init() methods)
			// when it is actually needed so do not instantiate it here. These
			// init() methods will not
			// be called unledd a SSL connection is attempted somewhere from
			// Taverna and it is inside them
			// that we actually call the initialize() method on Credential
			// Manager (and not from the Credential
			// Manager's constructor hence lazy)
			// getInstance();

			// Create Taverna's SSLSocketFactory and set the SSL socket factory
			// from HttpsURLConnectionS to use it
			HttpsURLConnection
					.setDefaultSSLSocketFactory(createTavernaSSLSocketFactory());
		}
	}

	/**
	 * Creates SSLSocketFactory based on Credential MAnager's Keystore and
	 * Truststore but only initalises Credential Manager when one of the methods
	 * needed for creating an HTTPS connection is invoked.
	 */
	private SSLSocketFactory createTavernaSSLSocketFactory() throws CMException {

		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSLv3");
		} catch (NoSuchAlgorithmException e1) {
			throw new CMException(
					"Failed to create SSL socket factory: the SSL algorithm was not available from any crypto provider.",
					e1);
		}

		KeyManager[] keyManagers = null;
		try {
			// Create our own KeyManager with (possibly not yet initialised)
			// Taverna's Keystore
			keyManagers = new KeyManager[] { new TavernaKeyManager() };
		} catch (Exception e) {
			throw new CMException(
					"Failed to create SSL socket factory: could not initiate SSL Key Manager.",
					e);
		}

		TrustManager[] trustManagers = null;
		try {
			// Create our own TrustManager with (possibly not yet initialised)
			// Taverna's Truststore
			trustManagers = new TrustManager[] { new TavernaTrustManager() };
		} catch (Exception e) {
			throw new CMException(
					"Failed to create SSL socket factory: could not initiate SSL Trust Manager.",
					e);
		}

		try {
			sc.init(keyManagers, trustManagers, new SecureRandom());
		} catch (KeyManagementException kmex) {
			throw new CMException("Failed to initiate the SSL socet factory",
					kmex);
		}
		// Set the default SSLContext to be used for subsequent SSL sockets from
		// Java
		SSLContext.setDefault(sc);
		// Create SSL socket to be used for HTTPS connections from the JVM
		// e.g. REST activity that uses Apache HTTP client library
		tavernaSSLSocketFactory = sc.getSocketFactory();

		return tavernaSSLSocketFactory;
	}

	public SSLSocketFactory getTavernaSSLSocketFactory() throws CMException {
		if (tavernaSSLSocketFactory == null) {
			return createTavernaSSLSocketFactory();
		} else {
			return tavernaSSLSocketFactory;
		}

	}

	/**
	 * Taverna's Key Manager is a customised X509KeyManager that initilises
	 * Credential Manager only if certain methods on it are invoked, i.e. if
	 * acces to Keystore is actually needed to authenticate the user.
	 */
	private class TavernaKeyManager extends X509ExtendedKeyManager {
		// The X509KeyManager as returned by the SunX509 provider, initialised
		// with the Keystore.
		X509KeyManager sunJSSEX509KeyManager = null;

		// Lazy initialisation - unless we are actually asked to do some SSL
		// stuff -
		// do not initialise Credential Manager as it will most probably result
		// in popping
		// the master password window, which we want to avoid early on while
		// Taverna is
		// starting, unless we need to contact a secure service early, e.g. to
		// populate Service Panel.
		private void init() throws Exception {

			logger.info("Credential Manager: inside TavernaKeyManager.init()");

			// Create a "default" JSSE X509KeyManager.
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509",
					"SunJSSE");

			if (!isInitialized) {
				logger.info("Credential Manager: inside TavernaKeyManager.init() - Credential Manager has not been instantiated yet.");
				// If we have not initialised the Credential Manager so far -
				// now is the time to do it
				try {
					initialize();
					logger.info("Credential Manager: inside TavernaKeyManager.init() - Credential Manager instantiated.");
				} catch (CMException cme) {
					throw new Exception(
							"Could not initialize Taverna's KeyManager for SSLSocketFactory: failed to initialise Credential Manager.");
				}
			}

			// Keystore and master password should not be null as we have just
			// initalised Credential Manager
			synchronized (keystore) {
				logger.info("Credential Manager: inside TavernaKeyManager.init() - Reinitialising the KeyManager.");

				kmf.init(keystore, masterPassword.toCharArray());

				KeyManager kms[] = kmf.getKeyManagers();
				/*
				 * Iterate over the returned KeyManagers, look for an instance
				 * of X509KeyManager. If found, use that as our "default" key
				 * manager.
				 */
				for (int i = 0; i < kms.length; i++) {
					if (kms[i] instanceof X509KeyManager) {
						sunJSSEX509KeyManager = (X509KeyManager) kms[i];
						return;
					}
				}
				// X509KeyManager not found - we have to fail the constructor.
				throw new Exception(
						"Could not initialize Taverna's KeyManager for SSLSocketFactory: could not find a SunJSSE X509 KeyManager.");
			}
		}

		public String chooseClientAlias(String[] keyType, Principal[] issuers,
				Socket socket) {
			logger.info("Credential Manager: inside chooseClientAlias()");

			// We have postponed initialisation until we are actually asked to
			// do something
			if (sunJSSEX509KeyManager == null) {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					return null;
				}
			}
			// Delegate the decision to the default key manager
			return sunJSSEX509KeyManager.chooseClientAlias(keyType, issuers,
					socket);
		}

		public String chooseServerAlias(String keyType, Principal[] issuers,
				Socket socket) {
			// TODO Auto-generated method stub
			return null;
		}

		public X509Certificate[] getCertificateChain(String alias) {
			logger.info("Credential Manager: inside getCertificateChain()");
			// We have postponed initialisation until we are actually asked to
			// do something
			if (sunJSSEX509KeyManager == null) {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					return null;
				}
			}
			// Delegate the decision to the default key manager
			return sunJSSEX509KeyManager.getCertificateChain(alias);
		}

		public String[] getClientAliases(String keyType, Principal[] issuers) {
			logger.info("Credential Manager: inside getClientAliases()");
			// We have postponed initialisation until we are actually asked to
			// do something
			if (sunJSSEX509KeyManager == null) {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					return null;
				}
			}
			// Delegate the decision to the default key manager
			return sunJSSEX509KeyManager.getClientAliases(keyType, issuers);
		}

		public PrivateKey getPrivateKey(String alias) {
			logger.info("Credential Manager: inside getPrivateKey()");
			// We have postponed initialisation until we are actually asked to
			// do something
			if (sunJSSEX509KeyManager == null) {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					return null;
				}
			}
			// Delegate the decision to the default key manager
			return sunJSSEX509KeyManager.getPrivateKey(alias);
		}

		public String[] getServerAliases(String keyType, Principal[] issuers) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**
	 * Taverna's Trust Manager is a customised X509TrustManager that initilises
	 * Credential Manager only if certain methods on it are invoked, i.e. if
	 * acces to Truststore is actually needed to authenticate the remote
	 * service.
	 */
	private class TavernaTrustManager implements X509TrustManager {

		/*
		 * The default X509TrustManager as returned by SunX509 provider,
		 * initialised with the Truststore. We delegate decisions to it, and
		 * fall back to ask the user if the default X509TrustManager does not
		 * trust the server's certificate.
		 */
		X509TrustManager sunJSSEX509TrustManager = null;

		// Lazy initialisation - unless we are actually asked to do some SSL
		// stuff -
		// do not initialise Credential Manager as it will most probably result
		// in popping
		// the master password window, which we want to avoid early on while
		// Taverna is
		// starting, unless we need to contact a secure service early, e.g. to
		// populate Service Panel.
		private void init() throws Exception {

			logger.info("Credential Manager: inside TavernaTrustManager.init()");

			// Create a "default" JSSE X509TrustManager.
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(
					"SunX509", "SunJSSE");

			if (!isInitialized) {
				logger.info("Credential Manager: inside TavernaTrustManager.init() - Credential Manager has not been instantiated yet.");
				// If we have not initialised the Credential Manager so far -
				// now is the time to do it
				try {
					initialize();
					logger.info("Credential Manager: inside Taverna TrustManager.init() - Credential Manager instantiated.");
				} catch (CMException cme) {
					throw new Exception(
							"Could not initialize Taverna's TrustManager for SSLSocketFactory: failed to initialise Credential Manager.");
				}
			}

			// Truststore should not be null as we have just initalised
			// Credential Manager above
			synchronized (truststore) {
				logger.info("Credential Manager: inside TavernaTrustManager.init() - Reinitialising the TrustManager.");
				SSLSocketFactory.getDefault();
				tmf.init(truststore);

				TrustManager tms[] = tmf.getTrustManagers();
				/*
				 * Iterate over the returned TrustManagers, look for an instance
				 * of X509TrustManager. If found, use that as our "default"
				 * trust manager.
				 */
				for (int i = 0; i < tms.length; i++) {
					if (tms[i] instanceof X509TrustManager) {
						sunJSSEX509TrustManager = (X509TrustManager) tms[i];
						return;
					}
				}

				// X509TrustManager not found - we have to fail the constructor.
				throw new Exception(
						"Could not initialize Taverna's TrustManager for SSLSocketFactory.");
			}
		}

		/*
		 * This method is called on the server-side for establishing trust with
		 * a client.
		 */
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		/*
		 * This method is called on the client-side for establishing trust with
		 * a server. We first try to delegate to the default trust manager that
		 * uses Taverna's Truststore. If that falls through we ask the user if
		 * they want to trust the certificate.
		 */
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {

			// We have postponed initialisation until we are actually asked to
			// do something
			if (sunJSSEX509TrustManager == null) {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
				}
			}
			// Delegate the decision to the default trust manager
			try {
				sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
			} catch (CertificateException excep) {
				// Pop up a dialog box asking whether to trust the server's
				// certificate chain.
				if (!shouldTrust(chain)) {
					throw excep;
				}
			}
		}

		public X509Certificate[] getAcceptedIssuers() {
			// We have postponed initialisation until we are actually asked to
			// do something
			if (sunJSSEX509TrustManager == null) {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					return null;
				}
			}
			return sunJSSEX509TrustManager.getAcceptedIssuers();
		}
	}

	/**
	 * Checks if a service is trusted and if not - asks user if they want to
	 * trust it.
	 */
	private boolean shouldTrust(final X509Certificate[] chain)
			throws IllegalArgumentException {
		if (chain == null || chain.length == 0) {
			throw new IllegalArgumentException(
					"At least one certificate needed in chain");
		}

		// If the certificate already exists in the truststore, it is implicitly
		// trusted
		// This will try to avoid prompting user twice as checkServerTrusted()
		// method gets called twice.
		// Well, this is not working - checkServerTrusted() is still called
		// twice.
		String alias = createTrustedCertificateAlias(chain[0]);
		try {
			if (truststore.containsAlias(alias)) {
				return true;
			}
		} catch (KeyStoreException e) {
			// Ignore
		}

		String name = chain[0].getSubjectX500Principal().getName();
		for (TrustConfirmationProvider trustConfirmationProvider : trustConfirmationProviders) {

			TrustConfirmation confirmation = trustConfirmationProvider
					.shouldTrustCertificate(chain);
			if (confirmation == null) {
				// SPI can't say yes or no, try next one
				continue;
			}
			if (confirmation.isShouldTrust()) {
				try {
					initialize(); // init the Credential Manager if needed
					addTrustedCertificate((X509Certificate) chain[0]);
					logger.info("Stored trusted certificate " + name);
				} catch (CMException ex) {
					logger.error("Credential Manager failed to "
							+ "save trusted certificate " + name, ex);
				}
			}
			if (logger.isDebugEnabled()) {
				if (confirmation.isShouldTrust()) {
					logger.debug("Trusting " + name + " according to "
							+ trustConfirmationProvider);
				} else {
					logger.debug("Not trusting " + name + " according to "
							+ trustConfirmationProvider);
				}
			}
			return confirmation.isShouldTrust();
		}
		logger.warn("No TrustConfirmationProvider instances could confirm or deny the trust in "
				+ name);
		// None of the trust confirmation providers (if there were any at all)
		// could confirm
		return false;
	}

	/**
	 * Normalize an URI for insertion as the basis for path-recursive lookups,
	 * ie. strip query and filename. For example: <code>
	 * URI uri = URI.create("http://foo.org/dir1/dirX/../dir2/filename.html?q=x")
	 * System.out.println(CredentialManager.normalizeServiceURI(uri));
	 * >>> http://foo.org/dir1/dir2/
	 * uri = URI.create("http://foo.org/dir1/dir2/");
	 * System.out.println(CredentialManager.normalizeServiceURI(uri));
	 * >>> http://foo.org/dir1/dir2/
	 * </code>
	 * <p>
	 * Note that #fragments are preserved, as these are used to indicate HTTP
	 * Basic Auth realms
	 * 
	 * @param serviceURI
	 *            URI for a service that is to be normalized
	 * @return A normalized URI without query, userinfo or filename, ie. where
	 *         uri.resolve(".").equals(uri).
	 */
	public URI normalizeServiceURI(URI serviceURI) {
		try {
			URI noUserInfo = setUserInfoForURI(serviceURI, null);
			URI normalized = noUserInfo.normalize();
			URI parent = normalized.resolve(".");
			// Strip userinfo, keep fragment
			URI withFragment = setFragmentForURI(parent,
					serviceURI.getFragment());
			return withFragment;
		} catch (URISyntaxException ex) {
			return serviceURI;
		}
	}

	public URI setFragmentForURI(URI uri, String fragment)
			throws URISyntaxException {
		return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
				uri.getPort(), uri.getPath(), uri.getQuery(), fragment);
	}

	public URI setUserInfoForURI(URI uri, String userinfo)
			throws URISyntaxException {
		return new URI(uri.getScheme(), userinfo, uri.getHost(), uri.getPort(),
				uri.getPath(), uri.getQuery(), uri.getFragment());
	}

	/**
	 * Reset the JVMs cache for authentication like HTTP Basic Auth.
	 * <p>
	 * Note that this method uses undocumented calls to
	 * <code>sun.net.www.protocol.http.AuthCacheValue</code> which might not be
	 * valid in virtual machines other than Sun Java 6. If these calls fail,
	 * this method will log the error and return <code>false</code>.
	 * 
	 * @return <code>true</code> if the JVMs cache could be reset, or
	 *         <code>false</code> otherwise.
	 */
	public boolean resetAuthCache() {

		// Sun should expose an official API to do this
		try {
			Class<?> AuthCacheValue = Class
					.forName("sun.net.www.protocol.http.AuthCacheValue");
			Class<?> AuthCacheImpl = Class
					.forName("sun.net.www.protocol.http.AuthCacheImpl");
			Class<?> AuthCache = Class
					.forName("sun.net.www.protocol.http.AuthCache");
			Method setAuthCache = AuthCacheValue.getMethod("setAuthCache",
					AuthCache);
			setAuthCache.invoke(null, AuthCacheImpl.newInstance());
			return true;
		} catch (Exception ex) {
			logger.warn(
					"Could not reset authcache, non-Sun JVM or internal Sun classes changed",
					ex);
			return false;
		}
	}

	/**
	 * Checks if the Keystore contains a username and password for the given
	 * service URI.
	 */
	public boolean hasUsernamePasswordForService(URI serviceURI)
			throws CMException {
		Map<URI, URI> mappedServiceURIs = getFragmentMappedURIsForAllUsernameAndPasswordPairs();
		for (URI possible : getPossibleServiceURIsToLookup(serviceURI, true)) {
			if (mappedServiceURIs.containsKey(possible)) {
				return true;
			}
		}
		return false;
	}

	private void loadDefaultConfigurationFiles() {
		if (credentialManagerDirectory == null) {
			credentialManagerDirectory = CMUtils
					.getCredentialManagerDefaultDirectory();
		}
		if (keystoreFile == null) {
			keystoreFile = new File(credentialManagerDirectory,
					KEYSTORE_FILE_NAME);
		}
		if (truststoreFile == null) {
			truststoreFile = new File(credentialManagerDirectory,
					TRUSTSTORE_FILE_NAME);
		}
	}

	/**
	 * Helper method to set the directory with Credential Manager's files. 
	 * We will use the OSGi Configuration Service for this in production.
	 * 
	 * @param credentialManagerDirectory
	 * @throws CMException
	 */
	public void setConfigurationDirectoryPath(File credentialManagerDirectory)
			throws CMException {

		if (credentialManagerDirectory == null) {
			throw new CMException("Credential Manager's configuration directory cannot be null.");
		}

		try {
			if (!credentialManagerDirectory.exists()) {
				credentialManagerDirectory.mkdir();
			}
		} catch (Exception e) {
			throw new CMException(
					"Failed to open Credential Manager's directory "+credentialManagerDirectory+" to load the security files: "
							+ e.getMessage(), e);
		}
			
		keystoreFile = new File(credentialManagerDirectory, KEYSTORE_FILE_NAME);
		truststoreFile = new File(credentialManagerDirectory,
				TRUSTSTORE_FILE_NAME);
	}

	// private void loadSecurityFiles(String credentialManagerDirPath)
	// throws CMException {
	//
	// // If credentialManagerDirPath is null (e.g. user did not specify -cmdir
	// on the command line)
	// // - try with Taverna's default one
	// if (credentialManagerDirPath == null){
	// credentialManagerDirectory =
	// CMUtils.getCredentialManagerDefaultDirectory();
	// }
	//
	// if (credentialManagerDirectory == null) {
	// try {
	// credentialManagerDirectory = new File(credentialManagerDirPath);
	// } catch (Exception e) {
	// throw new CMException(
	// "Failed to open Credential Manager's directory to load the security files: "
	// + e.getMessage(),
	// e);
	// }
	// }
	// if (keystoreFile == null){
	// keystoreFile = new File(credentialManagerDirectory, KEYSTORE_FILE_NAME);
	// }
	// if (truststoreFile == null){
	// truststoreFile = new File(credentialManagerDirectory,
	// TRUSTSTORE_FILE_NAME);
	// }
	// }

	// Clear the cached service URIs that have username and password associated
	// with them.
	// Basically we keep the list of all service URIs (and a map of servce URIs
	// to their URIs fragments
	// to find the realm for HTTP AuthN) that have a password entry in the
	// Keystore.
	public class ClearCachedServiceURIsObserver implements
			Observer<KeystoreChangedEvent> {
		public void notify(Observable<KeystoreChangedEvent> sender,
				KeystoreChangedEvent message) throws Exception {

			// Need to make sure we are initialized before we do anything else
			// as Credential Manager can be created but not initialized
			initialize();

			// If Keystore has changed - possibly some password entries have
			// changed
			// (could be key entries that have chabged but we do not know) - so
			// empty the service URI caches just in case.
			if (message.keystoreType.equals(KeystoreType.KEYSTORE)) {
				synchronized (keystore) {
					cachedServiceURIsMap = null;
					cachedServiceURIsList = null;
				}
			}
		}
	}

	// If any change to the Keystore or Truststore occurs -
	// create the new SSLSocketFactory and set the new default SSLContext
	// which is initialised with the updated Keystore and Truststore material
	public class KeystoreChangedObserver implements
			Observer<KeystoreChangedEvent> {
		public void notify(Observable<KeystoreChangedEvent> sender,
				KeystoreChangedEvent message) throws Exception {
			// Create the new SSLSocketFactory and set the default SSLContext
			// for
			// HTTPS connetions in the JVM
			HttpsURLConnection
					.setDefaultSSLSocketFactory(createTavernaSSLSocketFactory());
		}
	}

	/**
	 * Set the master password providers for providing the master password to
	 * encrypt/decrypt the Credential Maager's Keystore and Truststore.
	 * <p>
	 * This is done through the Spring DM.
	 */
	public void setMasterPasswordProviders(
			List<MasterPasswordProvider> masterPasswordProviders) {
		this.masterPasswordProviders = masterPasswordProviders;
	}
	
	/**
	 * Get the master password providers for providing the master password to
	 * encrypt/decrypt the Credential Maager's Keystore and Truststore.
	 * 
	 * @return the master password providers for providing the master password to
	 * encrypt/decrypt the Credential Maager's Keystore and Truststore.
	 * 
	 */
	public List<MasterPasswordProvider> getMasterPasswordProviders(){
		return masterPasswordProviders;
	}

	/**
	 * Set the Java truststore password providers for providing the password to
	 * encrypt/decrypt the Java's default truststore.
	 * <p>
	 * This is done through the Spring DM.
	 */
	public void setJavaTruststorePasswordProviders(
			List<JavaTruststorePasswordProvider> javaTruststorePasswordProvider) {
		this.javaTruststorePasswordProviders = javaTruststorePasswordProvider;
	}
	
	/**
	 * Get the Java truststore password providers for providing the password to
	 * encrypt/decrypt the Java's default truststore.
	 * 
	 * @return Java truststore providers for providing the password to
	 * encrypt/decrypt the Java's default truststore
	 */
	public List<JavaTruststorePasswordProvider> getJavaTruststorePasswordProviders(){
		return javaTruststorePasswordProviders;
	}
	
	/**
	 * Set the providers of username and passwords for services.
	 * <p>
	 * This is done through the Spring DM.
	 */
	public void setServiceUsernameAndPasswordProviders(
			List<ServiceUsernameAndPasswordProvider> serviceUsernameAndPasswordProviders) {
		this.serviceUsernameAndPasswordProviders = serviceUsernameAndPasswordProviders;
	}

	/**
	 * Get the providers of username and passwords for services.
	 * 
	 * @return the providers of username and passwords for services.
	 *
	 */
	public List<ServiceUsernameAndPasswordProvider> getServiceUsernameAndPasswordProviders(){
		return serviceUsernameAndPasswordProviders;
	}	
	
	/**
	 * Set the providers of trust confirmation for HTTPS connections to external services/sites.
	 * <p>
	 * This is done through the Spring DM.
	 */
	public void setTrustConfirmationProviders(
			List<TrustConfirmationProvider> trustConfirmationProviders) {
		this.trustConfirmationProviders = trustConfirmationProviders;
	}
	
	/**
	 * Get the providers of trust confirmation for HTTPS connections to external services/sites
	 * 
	 * @return the providers of trust confirmation for HTTPS connections to external services/sites
	 *
	 */
	public List<TrustConfirmationProvider> getTrustConfirmationProviders(){
		return trustConfirmationProviders;
	}

}
