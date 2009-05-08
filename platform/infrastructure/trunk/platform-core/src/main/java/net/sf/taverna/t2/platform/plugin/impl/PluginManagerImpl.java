package net.sf.taverna.t2.platform.plugin.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.sf.taverna.t2.platform.plugin.PluginDescriptionParser;
import net.sf.taverna.t2.platform.plugin.PluginException;
import net.sf.taverna.t2.platform.plugin.PluginIdentifier;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.SPIRegistry;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.ArtifactParseException;
import net.sf.taverna.t2.platform.pom.JarManager;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.util.download.DownloadException;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import net.sf.taverna.t2.platform.util.download.URLMapper;

/**
 * Implementation of PluginManager, reads persistant configuration from
 * [base]/pluginManager.plugins on startup and writes on modification to the
 * active or installed set of plugins.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class PluginManagerImpl implements PluginManager {

	// Cache of previously constructed class loaders
	private Map<PluginIdentifier, PluginClassLoader> loaderCache;

	// Cache of previously instantiated SPI registries by class
	private Map<Class<?>, SPIRegistryImpl<?>> spiRegistryCache;

	// A Raven instance, used to remove any system level artifacts and also to
	// determine the parent class loader for plug-in class loaders created by
	// this class
	private Raven raven;

	// Used to parse the plug-in description files and to determine the list of
	// jars and artifacts from which to build a plug-in class loader
	private PluginDescriptionParser pluginParser;

	// Used to fetch jars corresponding to artifacts
	private JarManager jarManager;

	// Base location for downloaded jar files, jar files will be stored at
	// [baseLocation]/pluginGroup/pluginId-pluginVersion/jars/jarName.jar
	private File baseJarLocation;

	// Download manager used to fetch jar files by URL
	private DownloadManager downloadManager;

	// Full list of all installed plug-ins, those that are resolved and for
	// which there are class loaders available, either cached or ready to
	// construct
	private Map<PluginIdentifier, PluginDescription> installedPlugins;

	// Plug-in packages which are currently active, that is to say providing
	// implementations to SPI registries
	private Set<PluginIdentifier> activePlugins;

	// Raven artifacts can be added to the plug-in manager and used to scan for
	// SPI implementations on startup, this is really only used to support
	// things like the hibernate sub-system of the reference manager
	private Set<ArtifactIdentifier> artifacts;

	public void setJarManager(JarManager jarManager) {
		this.jarManager = jarManager;
	}

	public void setPluginParser(PluginDescriptionParser pluginParser) {
		this.pluginParser = pluginParser;
	}

	public void setRaven(Raven raven) {
		this.raven = raven;
	}

	public void setBaseJarLocation(File location) {
		this.baseJarLocation = location;
	}

	public void setDownloadManager(DownloadManager manager) {
		this.downloadManager = manager;
	}

	public void setArtifactList(List<String> artifactAsString) {
		for (String s : artifactAsString) {
			artifacts.add(new ArtifactIdentifier(s));
		}
	}

	/**
	 * Construct a new plug-in manager implementation, use the various set
	 * methods to configure prior to use.
	 */
	public PluginManagerImpl() {
		this.loaderCache = new HashMap<PluginIdentifier, PluginClassLoader>();
		this.spiRegistryCache = new HashMap<Class<?>, SPIRegistryImpl<?>>();
		this.installedPlugins = new HashMap<PluginIdentifier, PluginDescription>();
		this.activePlugins = new HashSet<PluginIdentifier>();
		this.artifacts = new HashSet<ArtifactIdentifier>();
	}

	/**
	 * Load and, depending on configuration, activate plugins from a
	 * configuration file. This file is updated automatically, if not present
	 * this does nothing.
	 */
	public synchronized void loadConfiguration(List<URL> remoteRepositories) {
		File configurationFile = new File(baseJarLocation,
				"pluginManager.plugins");
		if (!configurationFile.exists()) {
			// File doesn't exist, do nothing.
			return;
		}
		List<PluginIdentifier> loadMe = new ArrayList<PluginIdentifier>();
		List<PluginIdentifier> installMe = new ArrayList<PluginIdentifier>();
		Reader reader = null;
		try {
			reader = new BufferedReader(new FileReader(configurationFile));
			Scanner scanner = new Scanner(reader);
			scanner.useDelimiter("\n");
			while (scanner.hasNext()) {
				String line = scanner.next().trim();
				if (line.startsWith("installed")) {
					// Installed but not active
					loadMe.add(new PluginIdentifier(line.split(" ")[1]));
				} else if (line.startsWith("active")) {
					installMe.add(new PluginIdentifier(line.split(" ")[1]));
				}
			}
			scanner.close();
			for (PluginIdentifier plugin : loadMe) {
				getPluginClassLoader(plugin, remoteRepositories);
			}
			for (PluginIdentifier plugin : installMe) {
				activatePlugin(plugin, remoteRepositories);
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe2) {
				//
			}
		}
	}

	private synchronized void writeConfiguration() {
		File configurationFile = new File(baseJarLocation,
				"pluginManager.plugins");
		if (configurationFile.exists()) {
			configurationFile.delete();
		}
		Writer outputStream = null;
		try {
			outputStream = new BufferedWriter(new FileWriter(configurationFile));
			outputStream.append("Written at " + new Date());
			for (PluginDescription desc : installedPlugins.values()) {
				if (activePlugins.contains(desc.getId())) {
					outputStream.append("\nactive ");
				} else {
					outputStream.append("\ninstalled ");
				}
				outputStream.append(desc.getId().toString());
			}
		} catch (IOException ioe) {
			// Unable to write configuration file
			ioe.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (IOException ioe2) {
				//
			}
		}
	}

	public synchronized void activatePlugin(PluginIdentifier id,
			List<URL> pluginRepositories) throws PluginException {
		if (activePlugins.contains(id)) {
			return;
		} else {
			// Ensure that the plugin is loaded
			ClassLoader cl = getPluginClassLoader(id, pluginRepositories);
			// Perform platform level initialization
			try {
				configurePluginFromPlatform(cl);
			} catch (Exception ex) {
				if (ex instanceof PluginException) {
					throw (PluginException) ex;
				} else {
					throw new PluginException(ex);
				}
			}
			activePlugins.add(id);
			for (SPIRegistryImpl<?> registry : spiRegistryCache.values()) {
				// Message the registry that the class loader has been added to
				// the active set
				registry.classLoaderAdded(cl);
			}
			writeConfiguration();
		}
	}

	/**
	 * Attempts to find a resource META-INF/platform.init - if this exists then
	 * each line is read to find a class name. The class name is then used to
	 * create a class object from the specified class loader, this class is
	 * searched for static set methods. The set methods on the class are then
	 * interrogated to determine the argument type of the parameter, and the
	 * getResourceForType method is used to obtain an appropriately typed
	 * object. This object is then used to call the setter method.
	 * 
	 * @param cl
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private synchronized void configurePluginFromPlatform(ClassLoader cl)
			throws IOException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Enumeration<URL> resources = cl.getResources("META-INF/platform.init");
		List<Class<?>> initClasses = new ArrayList<Class<?>>();
		while (resources.hasMoreElements()) {
			URL resourceURL = resources.nextElement();
			InputStream is;
			try {
				is = resourceURL.openStream();
			} catch (IOException ioe) {
				throw new PluginException("Unable to read platform.init file",
						ioe);
			}
			Scanner scanner = new Scanner(is);
			scanner.useDelimiter("\n");
			while (scanner.hasNext()) {
				String className = scanner.next().trim();
				if (className.length() == 0 || className.startsWith("#")) {
					continue;
				}
				try {
					Class<?> initClass = (Class<?>) cl.loadClass(className);
					initClasses.add(initClass);
				} catch (ClassNotFoundException cnfe) {
					throw new PluginException(
							"Unable to resolve platform initializer class",
							cnfe);
				}
			}
			scanner.close();
			try {
				is.close();
			} catch (IOException e) {
				continue;
			}
		}
		// Now have a list of class objects, search these for static set methods
		for (Class<?> initClass : initClasses) {
			for (Method method : initClass.getDeclaredMethods()) {
				int mods = method.getModifiers();
				if (Modifier.isStatic(mods) && !Modifier.isAbstract(mods)
						&& method.getName().startsWith("set")
						&& method.getParameterTypes().length == 1) {
					Class<?> argumentType = method.getParameterTypes()[0];
					Object argument = getResourceForType(argumentType);
					if (argument != null) {
						method.invoke(null, argument);
					}
				}
			}
		}
	}

	public abstract Object getResourceForType(Class<?> resourceType);

	public synchronized void deactivatePlugin(PluginIdentifier id) {
		if (!activePlugins.contains(id)) {
			return;
		} else {
			activePlugins.remove(id);
			ClassLoader cl = loaderCache.get(id);
			for (SPIRegistryImpl<?> registry : spiRegistryCache.values()) {
				// Message the registry that the class loader has been removed
				// from the active set
				registry.classLoaderRemoved(cl);
			}
			writeConfiguration();
		}
	}

	@SuppressWarnings("unchecked")
	public PluginIdentifier definingPlugin(Object o) {
		ClassLoader objectLoader;
		if (o instanceof Class) {
			objectLoader = ((Class) o).getClassLoader();
		} else {
			objectLoader = o.getClass().getClassLoader();
		}
		if (objectLoader instanceof PluginClassLoader) {
			return ((PluginClassLoader) objectLoader).id;
		} else {
			return null;
		}
	}

	public List<PluginDescription> getActivePluginList() {
		List<PluginDescription> result = new ArrayList<PluginDescription>();
		for (PluginDescription desc : installedPlugins.values()) {
			if (activePlugins.contains(desc.getId())) {
				result.add(desc);
			}
		}
		return result;
	}

	public List<PluginDescription> getCompletePluginList() {
		List<PluginDescription> result = new ArrayList<PluginDescription>();
		result.addAll(installedPlugins.values());
		return result;
	}

	public ClassLoader getPluginClassLoader(PluginIdentifier id,
			List<URL> pluginRepositories) throws PluginException {
		if (loaderCache.containsKey(id)) {
			return loaderCache.get(id);
		} else {
			return buildOrReturnPluginClassLoader(id, pluginRepositories);
		}
	}

	private synchronized ClassLoader buildOrReturnPluginClassLoader(
			PluginIdentifier id, List<URL> pluginRepositories)
			throws PluginException {
		// Check the cache first
		if (loaderCache.containsKey(id)) {
			return loaderCache.get(id);
		}
		// Assemble a list of URLs to the downloaded code in the form of
		// artifacts and jar files here.
		List<URL> codeURLs = new ArrayList<URL>();

		// Firstly get the plug-in description
		PluginDescription description = pluginParser.getDescription(id,
				pluginRepositories);

		// Get all artifacts, removing those in the system artifact list from
		// the raven instance
		Set<ArtifactIdentifier> systemArtifacts = raven.getSystemArtifactSet();

		for (ArtifactIdentifier artifactId : description.getArtifactList()) {
			// Skip artifacts that are defined as system, this means we already
			// have them and will inherit them from the parent class loader
			if (!systemArtifacts.contains(artifactId)) {
				try {
					List<URL> repositoryList = new ArrayList<URL>();
					repositoryList.addAll(description.getRepositoryList());
					repositoryList.addAll(raven.getDefaultRepositoryList());
					File artifactFile = jarManager.getArtifactJar(artifactId,
							repositoryList);
					codeURLs.add(artifactFile.toURI().toURL());
				} catch (DownloadException de) {
					throw new PluginException(
							"Unable to download artifact jar", de);
				} catch (ArtifactParseException ape) {
					throw new PluginException(
							"Unable to download artifact jar", ape);
				} catch (MalformedURLException e) {
					throw new PluginException(
							"Unable to build URL to downloaded file!", e);
				}
			}
		}
		// Do jar files next
		URLMapper mapper = new PluginJarMapper(id);
		for (URL jarURL : description.getJarList()) {
			List<URL> sources = new ArrayList<URL>();
			sources.add(jarURL);
			try {
				File jarFile = downloadManager.getAsFile(sources, null, mapper,
						1);
				codeURLs.add(jarFile.toURI().toURL());
			} catch (DownloadException de) {
				throw new PluginException("Unable to download jar file", de);
			} catch (MalformedURLException mue) {
				throw new PluginException(
						"Unable to construct URL to downloaded jar file!", mue);
			}
		}

		// Assemble a new URLClassLoader extension from the list of accessible
		// code artifacts, using the raven parent
		PluginClassLoader cl = new PluginClassLoader(codeURLs, raven
				.getParentClassLoader(), id);
		loaderCache.put(id, cl);
		installedPlugins.put(id, description);
		writeConfiguration();
		return cl;
	}

	@SuppressWarnings("unchecked")
	public <SPIType> SPIRegistry<SPIType> getSPIRegistry(Class<SPIType> spiClass) {
		if (spiRegistryCache.containsKey(spiClass)) {
			return (SPIRegistry<SPIType>) spiRegistryCache.get(spiClass);
		} else {
			return buildOrReturnSPIRegistry(spiClass);
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized <SPIType> SPIRegistry<SPIType> buildOrReturnSPIRegistry(
			Class<SPIType> spiClass) {
		// Check cache again first
		if (spiRegistryCache.containsKey(spiClass)) {
			return (SPIRegistry<SPIType>) spiRegistryCache.get(spiClass);
		}
		// Otherwise build a new SPI registry for the specified class, adding
		// the class loaders for any active plug-ins
		List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
		classLoaders.add(ClassLoader.getSystemClassLoader());
		// TODO: Should the class loader of the SPIType also be included? OUR
		// class loader?
		for (PluginDescription activePluginDesc : getActivePluginList()) {
			if (loaderCache.containsKey(activePluginDesc.getId())) {
				classLoaders.add(loaderCache.get(activePluginDesc.getId()));
			}
		}
		for (ArtifactIdentifier artifact : artifacts) {
			classLoaders.add(raven.getLoader(artifact, null));
		}
		return new SPIRegistryImpl<SPIType>(spiClass, classLoaders);
	}

	private class PluginClassLoader extends URLClassLoader {
		PluginIdentifier id;

		PluginClassLoader(List<URL> jarURLList, ClassLoader parent,
				PluginIdentifier id) {
			super(jarURLList.toArray(new URL[0]), parent);
			this.id = id;
		}

		@Override
		public String toString() {
			return "PluginClassLoader " + id;
		}

	}

	private class PluginJarMapper implements URLMapper {

		private File downloadDirectory;

		PluginJarMapper(PluginIdentifier id) {
			downloadDirectory = new File(
					PluginManagerImpl.this.baseJarLocation, "/plugins/"
							+ id.getGroupId() + "/" + id.getPluginId() + "-"
							+ id.getVersion());
			downloadDirectory.mkdirs();
		}

		public File map(URL source) {
			return new File(downloadDirectory, source.getFile());
		}

	}

}
