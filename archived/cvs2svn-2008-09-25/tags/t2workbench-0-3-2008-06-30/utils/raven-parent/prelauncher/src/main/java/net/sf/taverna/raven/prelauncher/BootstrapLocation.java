package net.sf.taverna.raven.prelauncher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Using code stolen from Taverna 1.4 to determine the startup location of the
 * bootstrap. This is used to figure out the application's installation
 * directory.
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * 
 */
public class BootstrapLocation {

	private static File bootstrapFile = null;

	/**
	 * Get the canonical directory of the class or jar file that this class was
	 * loaded. This method can be used to calculate the root directory of an
	 * installation.
	 * 
	 * @return the canonical directory of the class or jar file that this class
	 *         file was loaded from
	 * @throws IOException
	 *             if the canonical directory or jar file cannot be found
	 */
	public static File getBootstrapDir(Class theClass) throws IOException {
		File file = getBootstrapFile(theClass);
		if (file.isDirectory())
			return file;
		return file.getParentFile();
	}

	/**
	 * Get the canonical directory or jar file that this class was loaded from.
	 * 
	 * @return the canonical directory or jar file that this class file was
	 *         loaded from
	 * @throws IOException
	 *             if the canonical directory or jar file cannot be found
	 */
	public static File getBootstrapFile(Class theClass) throws IOException {
		if (bootstrapFile != null) {
			return bootstrapFile;
		}
		// Get a URL for where this class was loaded from
		String classResourceName = "/"
				+ theClass.getName().replace('.', '/')
				+ ".class";
		URL resource = theClass.getResource(classResourceName);
		if (resource == null)
			throw new IOException("Bootstrap file not found: "
					+ theClass.getName());
		String resourcePath = null;
		String embeddedClassName = null;
		String protocol = resource.getProtocol();
		boolean isJar = (protocol != null) && (protocol.equals("jar"));
		if (isJar) {
			resourcePath = URLDecoder.decode(resource.getFile(), "utf8");
			embeddedClassName = "!" + classResourceName;
		} else {
			resourcePath = URLDecoder.decode(resource.toExternalForm(), "utf8");
			embeddedClassName = classResourceName;
		}
		int sep = resourcePath.lastIndexOf(embeddedClassName);
		if (sep >= 0)
			resourcePath = resourcePath.substring(0, sep);

		URI fileURI;
		try {
			fileURI = new URI(resourcePath);
		} catch (URISyntaxException e) {
			throw new IOException("Invalid URI: " + resourcePath);
		}
		// Now that we have a URL, make sure that it is a "file" URL
		// as we need to coerce the URL into a File object
		if (!fileURI.getScheme().equals("file")) {
			throw new IOException("Bootstrap was not found in a file: "
					+ fileURI.getScheme());
		}
		// Coerce the URL into a File and check that it exists. Note that
		// the JVM <code>File(String)</code> constructor automatically
		// flips all '/' characters to '\' on Windows and there are no
		// valid escape characters so we would not have to worry about
		// URL encoded slashes.
		File file = new File(fileURI);
		if (!file.exists() || !file.canRead())
			throw new IOException("Bootstrap file not found: "
					+ theClass.getName());
		bootstrapFile = file.getCanonicalFile();

		return bootstrapFile;
	}
}
