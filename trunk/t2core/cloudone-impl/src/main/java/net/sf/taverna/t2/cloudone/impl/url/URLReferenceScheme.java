package net.sf.taverna.t2.cloudone.impl.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.bean.Beanable;

/**
 * Reference scheme defined by a URL. This URL can be global, site local or link
 * local. It makes use of the NetworkName and MachineName context entries to
 * determine validity
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class URLReferenceScheme implements ReferenceScheme, Beanable<String> {

	private URL url;

	public URLReferenceScheme() {
		url = null;
	}

	public URLReferenceScheme(URL url) {
		if (url == null) {
			throw new NullPointerException("URL can't be null");
		}
		this.url = url;
	}

	public InputStream dereference(DataManager manager)
			throws DereferenceException {
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new DereferenceException(e);
		}
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
		if (getMachineName(currentLocation.getLocationalContexts()).equals(
				getMachineName(contextSet))) {
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

	private static String getMachineName(Set<LocationalContext> context) {
		for (LocationalContext lc : context) {
			if (lc.getContextType().equals("MachineName")) {
				return lc.getValue("name");
			}
		}
		return null;
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

	public Date getExpiry() {
		return null;
	}

	public boolean isImmediate() {
		return false;
	}

	public String getAsBean() {
		return url.toExternalForm();
	}

	public void setFromBean(String url) throws IllegalArgumentException {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof URLReferenceScheme))
			return false;
		final URLReferenceScheme other = (URLReferenceScheme) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.toExternalForm().equals(other.url.toExternalForm()))
			return false;
		return true;
	}

}