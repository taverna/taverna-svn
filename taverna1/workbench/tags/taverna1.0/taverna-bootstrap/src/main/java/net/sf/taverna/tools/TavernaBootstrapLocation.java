package net.sf.taverna.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Using code stolen from Taverna 1.4 to determine the startup location of the bootstrap. This is used to
 * $taverna.startup which is used for default settings for a Taverna installation.
 * 
 * @author Stuart Owen
 *
 */
public class TavernaBootstrapLocation {
	
	private static File bootstrapFile = null;

	/**
     * Get the canonical directory of the class or jar file that this class was
     * loaded. This method can be used to calculate the root directory of an
     * installation.     
     *
     * @return the canonical directory of the class or jar file that this class
     *  file was loaded from
     * @throws IOException if the canonical directory or jar file
     *  cannot be found
     */
    public static File getBootstrapDir() throws IOException {
		File file = getBootstrapFile();
		if (file.isDirectory())
			return file;
		return file.getParentFile();
	}
    
    /**
     * Get the canonical directory or jar file that this class was loaded
     * from.
     *
     * @return the canonical directory or jar file that this class
     *  file was loaded from
     * @throws IOException if the canonical directory or jar file
     *  cannot be found
     */
    public static File getBootstrapFile() throws IOException {
        if (bootstrapFile != null) {
        		return bootstrapFile;
        }
            // Get a URL for where this class was loaded from
		String classResourceName = "/"
				+ TavernaBootstrapLocation.class.getName().replace('.', '/') + ".class";
		URL resource = TavernaBootstrapLocation.class.getResource(classResourceName);
		if (resource == null)
			throw new IOException("Bootstrap file not found: "
					+ TavernaBootstrapLocation.class.getName());
		String resourcePath = null;
		String embeddedClassName = null;
		boolean isJar = false;
		String protocol = resource.getProtocol();
		if ((protocol != null) && (protocol.indexOf("jar") >= 0)) {
			isJar = true;
		}
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

		// Now that we have a URL, make sure that it is a "file" URL
		// as we need to coerce the URL into a File object
		if (resourcePath.indexOf("file:") == 0)
			resourcePath = resourcePath.substring(5);
		else
			throw new IOException("Bootstrap file not found: "
					+ TavernaBootstrapLocation.class.getName());

		// Coerce the URL into a file and check that it exists. Note that
		// the JVM <code>File(String)</code> constructor automatically
		// flips all '/' characters to '\' on Windows and there are no
		// valid escape characters so we sould not have to worry about
		// URL encoded slashes.
		File file = new File(resourcePath);
		if (!file.exists() || !file.canRead())
			throw new IOException("Bootstrap file not found: "
					+ TavernaBootstrapLocation.class.getName());
		bootstrapFile = file.getCanonicalFile();

		return bootstrapFile;
    }
}
