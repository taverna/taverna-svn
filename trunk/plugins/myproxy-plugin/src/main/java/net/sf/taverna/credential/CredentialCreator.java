package net.sf.taverna.credential;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;
import org.globus.common.CoGProperties;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.tools.proxy.DefaultGridProxyModel;
import org.globus.tools.proxy.GridProxyModel;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Supports methods to create and delete credential and myproxy credential.
 * 
 * @author Bharathi Kattamuri
 * @param <JarURLInputStream>
 * 
 */
public class CredentialCreator {

	private static Logger logger = Logger.getLogger(CredentialCreator.class);

	private CoGProperties properties = null;
	private GridProxyModel proxyModel = null;
	private String tavernaHome = null;

	public final static int MYPROXY_PORT = 7512;
	/*
	 * These values have been set not to clash with other applications using cog
	 * api.
	 */
	public final static String CACERTS_DIR = "grid-certificates";
	public final static String PROXY_FILE = "proxyfile";
	private final static String certificatesZip = "certs.zip";

	/**
	 * In constructor certificates directory is set, and the directory is loaded
	 * with CA, root certificates method
	 */
	public CredentialCreator() {
		this.tavernaHome = System.getProperty("taverna.home");
		proxyModel = new DefaultGridProxyModel();
		properties = proxyModel.getProperties();
		properties.setCaCertLocations(this.getCACertLocation());
		try {
			this.loadCACertificates();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates GlobusCredential and writes into a file, is equivalent to
	 * grid-proxy-init command.
	 * 
	 * @param userCertFile
	 *            it is path to usercert.pem
	 * @param userKeyFile
	 *            it is path to userkey.pem
	 * @return GlobusCredential
	 * @throws Exception
	 */
	public GlobusCredential pemGridProxyInit(String gridpass,
			String userCertFile, String userKeyFile, String credentialTime)
			throws Exception {
		properties.setProxyLifeTime(Integer.parseInt(credentialTime.trim()));
		if (logger.isDebugEnabled())
			logger.debug("certfile:" + userCertFile + "keyfile:" + userKeyFile);

		if (logger.isDebugEnabled())
			logger.debug("proxyLifeTime is set");
		properties.setUserCertFile(userCertFile);
		properties.setUserKeyFile(userKeyFile);
		logger.debug("user certfile is: " + properties.getUserCertFile());
		GlobusCredential gc = null;

		gc = proxyModel.createProxy(gridpass);
		if (logger.isDebugEnabled())
			logger.debug("identitiy:" + gc.getIdentity() + " timeleft:"
					+ gc.getTimeLeft());
		this.createFile(gc);
		return gc;
	}

	/**
	 * Creates GlobusCredential and writes into a file, is equivalent to
	 * grid-proxy-init
	 * 
	 * @param gridpass
	 *            gridpassphrase
	 * @param pk12File
	 *            path to PKCS12 format file, which contain user certificate and
	 *            user key.
	 * @return
	 * @throws Exception
	 */
	public GlobusCredential pk12GridProxyInit(String gridpass, String pk12File,
			String credentialTime) throws Exception {

		properties.setProxyLifeTime(Integer.parseInt(credentialTime));
		java.security.Security
				.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyStore ks = KeyStore.getInstance("PKCS12", "BC");

		if (logger.isDebugEnabled())
			logger.debug("pk12file:" + pk12File);
		ks.load(new FileInputStream(pk12File), gridpass.toCharArray());
		Enumeration<String> en = ks.aliases();
		X509Certificate cert = null;
		PrivateKey key = null;
		String alias = null;
		while (en.hasMoreElements()) {
			alias = (String) en.nextElement();
			if (logger.isDebugEnabled())
				logger.debug("alias:" + alias);
			cert = (X509Certificate) ks.getCertificate(alias);
			if (cert.getBasicConstraints() == -1)
				break;
		}

		if (logger.isDebugEnabled())
			logger.debug("cert is:" + cert.getIssuerDN().getName());

		key = (PrivateKey) ks.getKey(alias, gridpass.toCharArray());
		X509Certificate[] chain = { cert };
		GlobusCredential gc = null;
		BouncyCastleCertProcessingFactory bcProcessingFactory = BouncyCastleCertProcessingFactory
				.getDefault();
		gc = bcProcessingFactory.createCredential(chain, key, 1024, (Integer
				.parseInt(credentialTime)) * 60 * 60,
				GSIConstants.DELEGATION_FULL);
		if (logger.isDebugEnabled())
			logger.debug("credential identity:" + gc.getIdentity()
					+ " timeleft:" + gc.getTimeLeft());
		this.createFile(gc);
		return gc;
	}

	/**
	 * Loads credential to myproxy server.
	 * 
	 * @param gc
	 * @param myproxyUsername
	 * @param myproxyPassphrase
	 * @param days
	 * @param hrs
	 * @param myproxyServer
	 * @return true if credential loaded successfully.
	 * @throws GSSException
	 * @throws MyProxyException
	 */
	public boolean createMyproxyCredential(GlobusCredential gc,
			String myproxyUsername, String myproxyPassphrase, String days,
			String hrs, String myproxyServer) throws GSSException,
			MyProxyException {

		GSSCredential gssCred = null;
		gssCred = new GlobusGSSCredentialImpl(gc,GSSCredential.INITIATE_AND_ACCEPT);

		MyProxy myproxy = new MyProxy(myproxyServer, MYPROXY_PORT);

		myproxy.put(gssCred, myproxyUsername, myproxyPassphrase, (Integer
				.parseInt(days) * 24 + Integer.parseInt(hrs)) * 60 * 60);
		GSSCredential myproxycred = null;
		myproxycred = myproxy.get(myproxyUsername, myproxyPassphrase, 300);
		if (myproxycred != null)
			return true;
		return false;
	}

	/**
	 * Writes credential to file specified in cog.properties, if not specified
	 * it will write to file in /tmp/x509xxxx
	 * 
	 * @param properties
	 * @param gc
	 * @return
	 * @throws IOException
	 */
	private boolean createFile(GlobusCredential gc) throws IOException {

		String proxyfile = properties.getProxyFile();
		logger.debug("proxyfile is:" + proxyfile);
		File tavernaHome = new File(System.getProperty("taverna.home"));
		File proxy = new File(tavernaHome, PROXY_FILE);
		proxy.createNewFile(); // creating a file in .taverna directory
		FileOutputStream out = new FileOutputStream(proxy); // writing proxy into a file
		gc.save(out);
		if (System.getProperty("os.name").equals("Linux")) {
			Runtime runtime = Runtime.getRuntime();
			String[] cmd = new String[] { "chmod", "600", proxyfile };
			Process process = null;
			try {
				process = runtime.exec(cmd, null);
				return (process.waitFor() == 0) ? true : false;
			} catch (Exception e) {
				return false;
			} finally {
				if (process != null) {
					try {
						process.getErrorStream().close();
					} catch (IOException e) {
					}
					try {
						process.getInputStream().close();
					} catch (IOException e) {
					}
					try {
						process.getOutputStream().close();
					} catch (IOException e) {
					}
				}
			}
		}
		return true;
	}

	/**
	 * Deletes proxyfile created from user certficates.
	 * 
	 * @return
	 */
	public boolean deleteCredential() {

		boolean fileDelete = false;
		File cFile = new File(tavernaHome +"/"+PROXY_FILE);
		if (cFile.exists())
			fileDelete = (cFile).delete();
		return fileDelete;
	}

	/**
	 * Deletes MyProxy credential from MyProxyServer
	 * 
	 * @param gc
	 * @param myproxyUsername
	 * @param myproxyPassphrase
	 * @param myproxyServer
	 * @return
	 * @throws GSSException
	 * @throws MyProxyException
	 */
	public boolean deleteMyProxyCredential(GlobusCredential gc,
			String myproxyUsername, String myproxyPassphrase,
			String myproxyServer) throws GSSException, MyProxyException {

		GSSCredential gssCred = null;
		GSSCredential myproxyCred = null;

		gssCred = new GlobusGSSCredentialImpl(gc,
				GSSCredential.INITIATE_AND_ACCEPT);

		MyProxy myproxy = new MyProxy(myproxyServer, MYPROXY_PORT);
		myproxyCred = myproxy.get(gssCred, myproxyUsername, myproxyPassphrase,
				100);  //retrieving myproxy to test whether proxy existed or not.
		if (myproxyCred == null)
			return false;
		myproxy.destroy(gssCred, myproxyUsername, myproxyPassphrase);
		return true;
	}

	/**
	 * creates GlobusCredential from already existing proxy.
	 * 
	 * @return GlobusCredential
	 * @throws GlobusCredentialException
	 */
	public GlobusCredential getGlobusCredentialFromFile()
			throws GlobusCredentialException {
		GlobusCredential gc = null;
		File proxyFile = new File(this.tavernaHome + "/" + PROXY_FILE);
		if (proxyFile.exists()) {
			gc = new GlobusCredential(this.tavernaHome + "/" + PROXY_FILE);
		}
		return gc;
	}

	/**
	 * Loads CAcertificates, Root certificates into cacertLocation from a zip
	 * file in the resources directory.
	 * 
	 * @throws IOException
	 */
	private void loadCACertificates() throws IOException {

		InputStream fis = CredentialCreator.class.getResourceAsStream("/"
				+ certificatesZip);
		FileOutputStream fos = new FileOutputStream(this.getCACertLocation()
				+ "/" + certificatesZip);
		this.write(fis, fos);

		ZipFile zfile = new ZipFile(this.getCACertLocation() + "/"
				+ certificatesZip);
		Enumeration<? extends ZipEntry> zentries = zfile.entries();

		while (zentries.hasMoreElements()) {
			ZipEntry zentry = (ZipEntry) zentries.nextElement();
			this.write(zfile.getInputStream(zentry), new BufferedOutputStream(
					new FileOutputStream(this.getCACertLocation() + "/"
							+ zentry.getName())));
		}

	}

	/**
	 * Creates a certficates directory, if one already not existed.
	 * 
	 * @return CACertLocatin
	 */
	private String getCACertLocation() {

		File certDir = new File(System.getProperty("taverna.home") + "/"
				+ CACERTS_DIR);
		if (!certDir.exists()) {
			certDir.mkdir();
		}
		return certDir.getPath();

	}

	private void write(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

}
