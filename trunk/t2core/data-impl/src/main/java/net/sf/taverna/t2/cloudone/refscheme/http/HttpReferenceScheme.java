package net.sf.taverna.t2.cloudone.refscheme.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.HeadMethod;

/**
 * Reference scheme defined by a URL. This URL can be global, site local or link
 * local. It makes use of the NetworkName and MachineName context entries to
 * determine validity
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class HttpReferenceScheme implements ReferenceScheme<HttpReferenceBean> {

	private static String getMachineName(Set<LocationalContext> context) {
		for (LocationalContext lc : context) {
			if (lc.getContextType().equals("MachineName")) {
				return lc.getValue("name");
			}
		}
		return null;
	}

	private static byte[] mask(byte[] address, byte[] mask) {
		byte[] masked = new byte[4];
		for (int i = 0; i < 4; i++) {
			masked[i] = (byte) (address[i] & mask[i]);
		}
		return masked;
	}

	private static boolean matches(byte[] a, byte[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Parse a dot seperated string into an array of bytes, assuming the address
	 * looks something like '192.34.53.22'.
	 * 
	 * @param address
	 * @return
	 */
	private static byte[] parseAddress(String address) {
		String[] networkParts = address.split("\\.");
		if (networkParts.length != 4) {
			throw new IllegalArgumentException(
					"Method must only be called with an address of the form '192.179.34.40'");
		}
		byte[] networkBytes = new byte[4];
		for (int i = 0; i < 4; i++) {
			networkBytes[i] = Byte.parseByte(networkParts[i]);
		}
		return networkBytes;
	}

	private URL url;

	public HttpReferenceScheme() {
		url = null;
	}

	public HttpReferenceScheme(URL url) {
		if (url == null) {
			throw new NullPointerException("URL can't be null");
		}
		if (!(url.getProtocol().equals("http"))
				|| url.getProtocol().equals("https")) {
			// throw new IllegalArgumentException("Only HTTP URLs are supported,
			// not " + url);
		}
		this.url = url;
	}

	/**
	 * Return the data at the {@link URL} represented by this
	 * {@link HttpReferenceScheme}
	 */
	public InputStream dereference(DataManager manager)
			throws DereferenceException {
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new DereferenceException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HttpReferenceScheme))
			return false;
		final HttpReferenceScheme other = (HttpReferenceScheme) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.toExternalForm().equals(other.url.toExternalForm()))
			return false;
		return true;
	}

	public HttpReferenceBean getAsBean() {
		HttpReferenceBean bean = new HttpReferenceBean();
		bean.setUrl(url.toExternalForm());
		return bean;
	}

	public Date getExpiry() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((url == null) ? 0 : url.toExternalForm().hashCode());
		return result;
	}

	public boolean isImmediate() {
		return false;
	}

	public void setFromBean(HttpReferenceBean bean)
			throws IllegalArgumentException {
		String url = bean.getUrl();
		if (this.url != null) {
			throw new IllegalStateException("Already initialised");
		}
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL " + url, e);
		}
	}

	@Override
	public String toString() {
		return "HttpReferenceScheme: " + url;
	}

	/**
	 * The reference is valid if :
	 * <ol>
	 * <li>The IP of the URL is global scope</li>
	 * <li>The IP of the URL is site-local scope and the DataManager which
	 * contains this reference instance is located on a machine with the same
	 * subnet as the target DataManager</li>
	 * <li>The IP of the URL is link-local the current and target DataManager
	 * must have identical machine-name keys</li>
	 * </ol>
	 */
	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		// First check whether we're on the same machine (as identified by
		// UUID) as the target, in which case all URLs are by definition valid.
		String currentMachineName = getMachineName(currentLocation
				.getLocationalContexts());
		if (currentMachineName != null
				&& currentMachineName.equals(getMachineName(contextSet))) {
			return true;
		}

		InetAddress referenceAddress;
		try {
			referenceAddress = InetAddress.getByName(url.getHost());
		} catch (UnknownHostException e) {
			// FIXME log the problem here in some way
			return false;
		}
		byte[] referenceAddressBytes = referenceAddress.getAddress();

		if (referenceAddress.isSiteLocalAddress()) {
			// Have an address on a non global subnet
			for (LocationalContext lc : currentLocation.getLocationalContexts()) {
				if (lc.getContextType().equals("NetworkName")) {
					String subnet = lc.getValue("subnet");
					byte[] networkBytes = parseAddress(subnet);
					String mask = lc.getValue("mask");
					byte[] maskBytes = parseAddress(mask);
					boolean matches = matches(mask(networkBytes, maskBytes),
							mask(referenceAddressBytes, maskBytes));
					if (matches) {
						String referenceNetworkName = lc.getValue("name");
						// Now search for the specified network name in the
						// context set of the target, if it exists then we're
						// good and can return true, if it doesn't we don't do
						// anything and try the next network that the reference
						// could belong to (almost certainly won't be one!)
						for (LocationalContext targetContext : contextSet) {
							if (targetContext.getContextType().equals(
									"NetworkName")) {
								if (targetContext.getValue("name").equals(
										referenceNetworkName)) {
									return true;
								}
							}
						}
					}

				}
			}
			return false;
		} else if (referenceAddress.isLinkLocalAddress()) {
			// We're not on the same machine so always return false
			return false;
		} else {
			// If we get here the address is neither link local or site local,
			// the only other option here is that the address of the reference
			// is global. Even if the target doesn't have access to the global
			// internet the reference is still valid (although in this case it
			// wouldn't be possible to dereference it)
			return true;
		}
	}

	public URL getUrl() {
		return url;
	}

	public String getCharset() throws DereferenceException {
		if (!url.getProtocol().equals("http")) {
			return null; // Don't know
		}
		HeadMethod method = new HeadMethod(url.toExternalForm());
		HttpClient httpClient = new HttpClient();
		try {
			httpClient.executeMethod(method);
			return method.getResponseCharSet();
		} catch (HttpException e) {
			throw new DereferenceException(e);
		} catch (IOException e) {
			throw new DereferenceException(e);
		} finally {
			method.releaseConnection();
		}
	}
}