/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.localworker;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.taverna.server.localworker.remote.RemoteSecurityContext;
import org.taverna.server.master.common.Credential;
import org.taverna.server.master.exceptions.InvalidCredentialException;
import org.taverna.server.master.utils.UsernamePrincipal;
import org.taverna.server.master.utils.X500Utils;

/**
 * Factoring out of the part of the security context handling that actually
 * deals with the different types of credentials.
 * 
 * @author Donal Fellows
 */
class SecurityContextDelegateImpl extends SecurityContextDelegate {
	private Log log = LogFactory.getLog("Taverna.Server.LocalWorker");
	private static final char USERNAME_PASSWORD_SEPARATOR = '\u0000';
	private static final String USERNAME_PASSWORD_KEY_ALGORITHM = "DUMMY";
	/** What passwords are encoded as. */
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private X500Utils x500Utils;
	private transient String helioToken;

	/**
	 * Initialise the context delegate.
	 * 
	 * @param run
	 *            What workflow run is this for?
	 * @param owner
	 *            Who owns the workflow run?
	 * @param factory
	 *            What class built this object?
	 */
	protected SecurityContextDelegateImpl(RemoteRunDelegate run,
			UsernamePrincipal owner, SecurityContextFactory factory) {
		super(run, owner, factory);
		this.x500Utils = factory.x500Utils;
	}

	@Override
	public void validateCredential(Credential c)
			throws InvalidCredentialException {
		try {
			if (c instanceof Credential.CaGridProxy)
				validateCaGridPoxyCredential((Credential.CaGridProxy) c);
			else if (c instanceof Credential.Password)
				validatePasswordCredential((Credential.Password) c);
			else if (c instanceof Credential.KeyPair)
				validateKeyCredential((Credential.KeyPair) c);
			else
				throw new InvalidCredentialException("unknown credential type");
		} catch (InvalidCredentialException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidCredentialException(e);
		}
	}

	@Override
	public void addCredentialToKeystore(Credential c) throws KeyStoreException {
		try {
			if (c instanceof Credential.Password)
				addUserPassToKeystore((Credential.Password) c);
			else if (c instanceof Credential.CaGridProxy)
				addCaGridPoxyToKeystore((Credential.CaGridProxy) c);
			else if (c instanceof Credential.KeyPair)
				addKeypairToKeystore((Credential.KeyPair) c);
			else
				throw new KeyStoreException("unknown credential type");
		} catch (KeyStoreException e) {
			throw e;
		} catch (Exception e) {
			throw new KeyStoreException(e);
		}
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

	/**
	 * Tests whether the given username+password credential descriptor is valid.
	 * If it is invalid, an exception will be thrown describing what the problem
	 * is. Validation mainly consists of listing what the username is.
	 * 
	 * @param passwordDescriptor
	 *            The credential descriptor to validate.
	 * @throws InvalidCredentialException
	 *             If the username is empty. NB: the password may be empty!
	 *             That's legal (if unwise).
	 */
	protected void validatePasswordCredential(
			Credential.Password passwordDescriptor)
			throws InvalidCredentialException {
		if (passwordDescriptor.username == null
				|| passwordDescriptor.username.trim().isEmpty())
			throw new InvalidCredentialException("absent or empty username");
		String keyToSave = passwordDescriptor.username
				+ USERNAME_PASSWORD_SEPARATOR + passwordDescriptor.password;
		passwordDescriptor.loadedKey = encodeKey(keyToSave);
		passwordDescriptor.loadedTrustChain = null;
	}

	private static Key encodeKey(String key) {
		return new SecretKeySpec(key.getBytes(UTF8),
				USERNAME_PASSWORD_KEY_ALGORITHM);
	}

	/**
	 * Adds a username/password credential pair to the current keystore.
	 * 
	 * @param c
	 *            The username and password.
	 * @throws KeyStoreException
	 */
	protected void addUserPassToKeystore(Credential.Password c)
			throws KeyStoreException {
		String alias = "password#" + c.serviceURI;
		addKeypairToKeystore(alias, c);
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

	/**
	 * Tests whether the given key-pair credential descriptor is valid. If it is
	 * invalid, an exception will be thrown describing what the problem is.
	 * 
	 * @param keypairDescriptor
	 *            The descriptor to validate.
	 * @throws InvalidCredentialException
	 *             If the descriptor is invalid
	 * @throws KeyStoreException
	 *             If we don't understand the keystore type or the contents of
	 *             the keystore
	 * @throws NoSuchAlgorithmException
	 *             If the keystore is of a known type but we can't comprehend
	 *             its security
	 * @throws CertificateException
	 *             If the keystore does not include enough information about the
	 *             trust chain of the keypair
	 * @throws UnrecoverableKeyException
	 *             If we can't get the key out of the keystore
	 * @throws IOException
	 *             If we can't read the keystore for prosaic reasons (e.g., file
	 *             absent)
	 */
	protected void validateKeyCredential(Credential.KeyPair keypairDescriptor)
			throws InvalidCredentialException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException {
		if (keypairDescriptor.credentialName == null
				|| keypairDescriptor.credentialName.trim().isEmpty())
			throw new InvalidCredentialException(
					"absent or empty credentialName");

		InputStream contentsAsStream;
		if (keypairDescriptor.credentialBytes != null
				&& keypairDescriptor.credentialBytes.length > 0) {
			contentsAsStream = new ByteArrayInputStream(
					keypairDescriptor.credentialBytes);
			keypairDescriptor.credentialFile = null;
		} else if (keypairDescriptor.credentialFile == null
				|| keypairDescriptor.credentialFile.trim().isEmpty())
			throw new InvalidCredentialException(
					"absent or empty credentialFile");
		else {
			contentsAsStream = contents(keypairDescriptor.credentialFile);
			keypairDescriptor.credentialBytes = new byte[0];
		}
		if (keypairDescriptor.fileType == null
				|| keypairDescriptor.fileType.trim().isEmpty())
			keypairDescriptor.fileType = KeyStore.getDefaultType();
		keypairDescriptor.fileType = keypairDescriptor.fileType.trim();

		KeyStore ks = KeyStore.getInstance(keypairDescriptor.fileType);
		char[] password = keypairDescriptor.unlockPassword.toCharArray();
		ks.load(contentsAsStream, password);

		try {
			keypairDescriptor.loadedKey = ks.getKey(
					keypairDescriptor.credentialName, password);
		} catch (UnrecoverableKeyException ignored) {
			keypairDescriptor.loadedKey = ks.getKey(
					keypairDescriptor.credentialName, new char[0]);
		}
		if (keypairDescriptor.loadedKey == null)
			throw new InvalidCredentialException(
					"no such credential in key store");
		keypairDescriptor.loadedTrustChain = ks
				.getCertificateChain(keypairDescriptor.credentialName);
		if (keypairDescriptor.loadedTrustChain == null
				|| keypairDescriptor.loadedTrustChain.length == 0)
			throw new InvalidCredentialException(
					"could not establish trust chain for credential");
	}

	/**
	 * Adds a key-pair to the current keystore.
	 * 
	 * @param c
	 *            The key-pair.
	 * @throws KeyStoreException
	 */
	protected void addKeypairToKeystore(Credential.KeyPair c)
			throws KeyStoreException {
		X509Certificate subjectCert = (X509Certificate) c.loadedTrustChain[0];
		String alias = "keypair#"
				+ getPrincipalName(subjectCert.getSubjectX500Principal()) + "#"
				+ getPrincipalName(subjectCert.getIssuerX500Principal()) + "#"
				+ x500Utils.getSerial(subjectCert);
		addKeypairToKeystore(alias, c);
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

	private void validateCaGridPoxyCredential(Credential.CaGridProxy c)
			throws InvalidCredentialException, UnrecoverableKeyException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException {
		// Proxies are just normal credentials at this point
		validateKeyCredential(c);

		if (c.authenticationService.toString().isEmpty())
			throw new InvalidCredentialException(
					"missing authenticationService");
		if (c.dorianService.toString().length() == 0)
			throw new InvalidCredentialException("missing dorianService");
	}

	private void addCaGridPoxyToKeystore(Credential.CaGridProxy c)
			throws KeyStoreException {
		String alias = "cagridproxy#" + c.authenticationService + " "
				+ c.dorianService;
		addKeypairToKeystore(alias, c);
	}

	// FIXME Use agreed header name for HELIO CIS token
	private static final String HELIO_CIS_TOKEN = "X-Helio-CIS";

	@Override
	public void initializeSecurityFromSOAPContext(MessageContext context) {
		// does nothing
		@SuppressWarnings("unchecked")
		Map<String, List<String>> headers = (Map<String, List<String>>) context
				.get(HTTP_REQUEST_HEADERS);
		if (factory.supportHelioToken && headers.containsKey(HELIO_CIS_TOKEN)) {
			helioToken = headers.get(HELIO_CIS_TOKEN).get(0);
		}
	}

	@Override
	public void initializeSecurityFromRESTContext(HttpHeaders context) {
		// does nothing
		MultivaluedMap<String, String> headers = context.getRequestHeaders();
		if (factory.supportHelioToken && headers.containsKey(HELIO_CIS_TOKEN)) {
			helioToken = headers.get(HELIO_CIS_TOKEN).get(0);
		}
	}

	@Override
	protected void conveyExtraSecuritySettings(RemoteSecurityContext rc) throws RemoteException {
		try {
			if (helioToken != null) {
				log.info("transfering HELIO CIS token: " + helioToken);
				rc.setHelioToken(helioToken);
			}
		} finally {
			helioToken = null;
		}
	}
}
