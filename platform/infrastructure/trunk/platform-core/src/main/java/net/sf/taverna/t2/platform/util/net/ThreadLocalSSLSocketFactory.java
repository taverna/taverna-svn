package net.sf.taverna.t2.platform.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * An implementation of SSLSocketFactory which delegates to one of two internal
 * SSLSocketFactory implementations. The implementation delegated to is
 * determined by a thread local property and is either the default SSL socket or
 * a customized one with a trust manager that allows the use of unsigned server
 * certificates.
 * <p>
 * To set this as the default call install() prior to making any HTTPS
 * connections, then bracket code which needs to trust unsigned certificates in
 * the startTrustingEverything() and stopTrustingEverything() methods.
 * 
 * @author Tom Oinn
 * 
 */
class ThreadLocalSSLSocketFactory extends SSLSocketFactory {

	/**
	 * Calls to open HTTPS connections will trust unsigned certificates afer
	 * this call is made, this is scoped to the current thread only.
	 */
	public static void startTrustingEverything() {
		threadLocalFactory.set(createAlwaysTrustingFactory());
	}

	/**
	 * Stop trusting unsigned certificates, reverting to the default behaviour
	 * for the current thread.
	 */
	public static void stopTrustingEverything() {
		threadLocalFactory.set(null);
	}

	/**
	 * Set this as the default global socket factory for HTTPS connections
	 */
	public static void install() {
		HttpsURLConnection
				.setDefaultSSLSocketFactory(new ThreadLocalSSLSocketFactory());
	}

	/**
	 * Determine whether the current thread will trust unsigned certificates
	 */
	public static boolean isTrustingEverything() {
		return (threadLocalFactory.get() != null);
	}

	/**
	 * Never construct manually
	 */
	private ThreadLocalSSLSocketFactory() {
		super();
	}

	private static ThreadLocal<SSLSocketFactory> threadLocalFactory = new ThreadLocal<SSLSocketFactory>();

	private static SSLSocketFactory createAlwaysTrustingFactory() {
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e1) {

			e1.printStackTrace();
		}
		TrustManager overlyTrusting = new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {

			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {

			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};
		try {
			sc.init(null, new TrustManager[] { overlyTrusting },
					new SecureRandom());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return sc.getSocketFactory();

	}

	private SSLSocketFactory getFactory() {
		if (threadLocalFactory.get() == null) {
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		} else {
			return threadLocalFactory.get();
		}
	}

	@Override
	public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
			throws IOException {
		return getFactory().createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return getFactory().getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return getFactory().getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket() throws IOException {
		return getFactory().createSocket();
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException,
			UnknownHostException {
		return getFactory().createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return getFactory().createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		return getFactory().createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
			int arg3) throws IOException {
		return getFactory().createSocket(arg0, arg1, arg2, arg3);
	}
}