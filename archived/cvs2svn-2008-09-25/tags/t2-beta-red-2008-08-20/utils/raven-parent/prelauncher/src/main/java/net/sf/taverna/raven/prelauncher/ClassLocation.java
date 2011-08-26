package net.sf.taverna.raven.prelauncher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.WeakHashMap;

/**
 * Given a {@link Class}, find the directory/JAR it was loaded from. This can
 * be used to find the bootstrap directory the application was installed to.
 * 
 * @author Stian Soiland-Reyes
 * @author Stuart Owen
 * 
 */
public class ClassLocation {

	private static WeakHashMap<Class<?>, File> classLocationFiles = new WeakHashMap<Class<?>, File>();
	private static WeakHashMap<Class<?>, File> classLocationDirs = new WeakHashMap<Class<?>, File>();
	private static WeakHashMap<Class<?>, URI> classLocationURIs = new WeakHashMap<Class<?>, URI>();

	/**
	 * Get the canonical directory of the class file or jar file that the given
	 * class was loaded from. This method can be used to calculate the root
	 * directory of an installation.
	 * 
	 * @see #getClassLocationFile(Class)
	 * 
	 * @param theClass
	 *            The class which location is to be found
	 * @return The canonical directory of the class or jar file that this class
	 *         file was loaded from
	 * @throws IOException
	 *             if the canonical directory or jar file cannot be found
	 */
	public static File getClassLocationDir(Class<?> theClass)
			throws IOException {
		if (classLocationDirs.containsKey(theClass)) {
			return classLocationDirs.get(theClass);
		}
		File file = getClassLocationFile(theClass);
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}
		classLocationDirs.put(theClass, file);
		return file;
	}

	/**
	 * Get the canonical directory or jar file that the given class was loaded
	 * from. Note that this file might be a jar, use
	 * {@link #getClassLocationDir(Class)} if you want the directory that
	 * contains the JAR.
	 * 
	 * @see #getClassLocationURI(Class)
	 * @return The canonical directory or jar file that this class file was
	 *         loaded from
	 * @throws IOException
	 *             if the canonical directory or jar file cannot be found, or
	 *             the class was not loaded from a file:/// URI.
	 */
	public static File getClassLocationFile(Class<?> theClass)
			throws IOException {
		if (classLocationFiles.containsKey(theClass)) {
			return classLocationFiles.get(theClass);
		}

		URI fileURI = getClassLocationURI(theClass);
		// Now that we have a URL, make sure that it is a "file" URL
		// as we need to coerce the URL into a File object
		if (!fileURI.getScheme().equals("file")) {
			throw new IOException("Class " + theClass
					+ " was not loaded from a file, but from " + fileURI);
		}
		// Coerce the URL into a File and check that it exists. Note that
		// the JVM <code>File(String)</code> constructor automatically
		// flips all '/' characters to '\' on Windows and there are no
		// valid escape characters so we would not have to worry about
		// URL encoded slashes.
		File file = new File(fileURI);
		if (!file.exists() || !file.canRead())
			throw new IOException("File/directory " + file + " where "
					+ theClass + " was loaded from was not found");
		File loadingFile = file.getCanonicalFile();
		classLocationFiles.put(theClass, loadingFile);
		return loadingFile;
	}

	/**
	 * Get the URI from where the given class was loaded from. Note that this
	 * might be pointing to a JAR or a location on the network. If you want the
	 * location as a file or directory, use {@link #getClassLocationFile(Class)}
	 * or {@link #getClassLocationDir(Class)}.
	 * 
	 * @see #getClassLocationURI(Class)
	 * @return The canonical directory or jar file that this class file was
	 *         loaded from
	 * @throws IOException
	 *             if the canonical directory or jar file cannot be found, or
	 *             the class was not loaded from a file:/// URI.
	 */
	public static URI getClassLocationURI(Class<?> theClass) throws IOException {
		if (classLocationURIs.containsKey(theClass)) {
			return classLocationURIs.get(theClass);
		}

		// Get a URL for where this class was loaded from
		String classResourceName = theClass.getName().replace('.', '/')
				+ ".class";
		URL resource = theClass.getResource("/" + classResourceName);
		if (resource == null)
			throw new IOException("Source of class " + theClass + " not found");
		String resourcePath = null;
		String embeddedClassName = null;
		String protocol = resource.getProtocol();
		boolean isJar = (protocol != null) && (protocol.equals("jar"));
		if (isJar) {
			// Note: DON'T decode as the part-URL is not double-encoded
			// and otherwise %20 -> " " -> new URI() would fail
			resourcePath = resource.getFile();
			embeddedClassName = "!/" + classResourceName;
		} else {
			resourcePath = resource.toExternalForm();
			embeddedClassName = classResourceName;
		}
		int sep = resourcePath.lastIndexOf(embeddedClassName);
		if (sep >= 0) {
			resourcePath = resourcePath.substring(0, sep);
		}

		URI sourceURI;
		try {
			sourceURI = new URI(resourcePath).normalize();
		} catch (URISyntaxException e) {
			throw new IOException("Invalid URI: " + resourcePath);
		}
		classLocationURIs.put(theClass, sourceURI);
		return sourceURI;
	}
}
