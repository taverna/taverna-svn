package net.sf.taverna.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import net.sf.taverna.tools.AbstractConfiguration;
import net.sf.taverna.tools.Bootstrap;

import org.apache.log4j.Logger;

/**
 * myGrid configuration, such as services to load in the workbench or LSID
 * provider to use. The configuration is in the file mygrid.properties in the
 * classpath.
 * <p>
 * The recommended way to get the configuration is to call the static methods
 * getProperties() and getProperty().
 * <p>
 * For backwards compatability, loadMygridProperties() loads the mygrid
 * properties into the system properties. This was previously done by a static
 * block in org.embl.ebi.escience.scuflui.workbench.Workbench. This method
 * should be called by the main() methods to allow legacy classes to retrieve
 * myGrid configuration.
 * 
 * @author Stian Soiland
 * 
 */
public class MyGridConfiguration extends AbstractConfiguration {

	private static MyGridConfiguration instance = null;
	private static Log4JConfiguration log4j = null;
	private static Logger logger = Logger.getLogger(MyGridConfiguration.class);
	
	private MyGridConfiguration() {
		initialiseProperties();
	}
	
	/**
	 * Provides access to the singleton instance of the MygridConfiguration, though most access will normally
	 * occur through the static methods rather than to the instance directly.
	 * @return
	 */
	public static MyGridConfiguration getInstance() {
		if (instance==null) {
			instance=new MyGridConfiguration();
			if (instance.getProperties()!=null) {
				System.getProperties().putAll(instance.getProperties());
			}
			
			if (log4j==null) log4j=new Log4JConfiguration();
		}
		
		return instance;
	}
	
	@Override
	protected String getConfigurationFilename() {
		return "mygrid.properties";
	}

	/**
	 * Look up a myGrid property.
	 * <p>
	 * This is a shorthand for <code>getProperties().getProperty(key)</code>. 
	 * Use this instead of the legacy <code>System.getProperty("taverna.X")</code>,
	 * which depends on <code>loadMygridProperties()</code> being previously called.
	 * 
	 * @param key
	 * @return value
	 */
	public static String getProperty(String key) {
		Properties p = getInstance().getProperties();
		if (p == null) {
			logger.warn("mygrid properties not found, returning null for "
					+ key);
			return null;
		}
		return p.getProperty(key);
	}
	
	/**
	 * Tries to open a stream to the mygrid.properties on the classpath if it cannot be found by the super-class.
	 * (This is primarily for use during testing).
	 */
	protected InputStream getInputStream() {
		InputStream result = super.getInputStream();
		if (result==null) { //resort to using mygrid.properties from the classpath
			result=MyGridConfiguration.class.getResourceAsStream("/conf/mygrid.properties");
			if (result!=null) logger.info("mygrid.properties file couldn't be determined, falling back to finding on the classpath");
		}
		return result;
	}
	
	@Override
	protected boolean isSystemOverrided() {
		return true;
	}


	/**
	 * Look up a myGrid property.
	 * <p>
	 * Like getProperty(String key) - but return <code>def</code>
	 * if the key is unknown.
	 * 
	 * @param key
	 * @param def Default string if key not found
	 * @return
	 */
	public static String getProperty(String key, String def) {
		String value = getProperty(key);
		if (value == null) {
			return def;
		} 
		return value;
	}

	/**
	 * Flush cache of properties. Next call to getProperties() or getProperty()
	 * will force a reload from files
	 * 
	 */
	public static void flushProperties() {
		getInstance().flush();
	}
//
	/**
	 * Load the myGrid properties and store them into the System properties. 
	 * This function should be called as early as possible in your program.
	 * <p>
	 * This is provided for backwards compatibility <em>only</em>, as old
	 * code relies on System.getProperties() to be prepopulated with the myGrid
	 * configuration.
	 * <p>
	 * Do not rely on properties to have been loaded globally, instead use
	 * MyGridConfiguration.getProperties()
	 * 
	 * @see MyGridConfiguration.getProperties()
	 * 
	 */
	@Deprecated
	synchronized public static void loadMygridProperties() {
		MyGridConfiguration.getInstance(); //requesting the instance forces it to load.
	}

	/**
	 * Get the user's application directory according to taverna.home.
	 * <p>
	 * The system property <code>taverna.home</code> is assumed to have been
	 * set by Bootstrap.findUserDir() or externally. The directory is created
	 * if needed.
	 * 
	 * @see net.sf.taverna.tools.Bootstrap.findUserDir()
	 * @return <code>File</code> object representing the user
	 *         directory for this application, or <code>null</code> if it could
	 *         not be found or created.
	 */
	public static File getUserDir() {
		String tavernaHome = getInstance().getTavernaHome();
		if (tavernaHome == null) {
			Bootstrap.findUserDir();
			tavernaHome = System.getProperty("taverna.home");
			if (tavernaHome == null) {
				logger.error("Could not find Taverna home. Try setting -Dtaverna.home");
				return null;
			}
		}
		File dir = new File(tavernaHome);
		dir.mkdirs();
		if (! dir.isDirectory()) {
			logger.warn("Invalid taverna.home directory " + dir);
			return null;
		}
		return dir;
	}
	
	/**
	 * Returns a File representing the Taverna startup directory. This is the directory
	 * containing the startup script plus default configuration directories.
	 * 
	 * This directory is determined by $taverna.startup. Returns null if this is not defined.
	 * @return
	 */
	public static File getStartupDir() {
		File result = null;
		String startup=getProperty("taverna.startup");
		if (startup!=null) {
			result=new File(startup);
		}
		return result;
	}
	
	/**
	 * Returns a File representing a subfolder within the Taverna startup directory. 
	 * If no startup directory is defined, then null is returned.
	 * 
	 * There is no attempt to create the directory if it doesn't exist, since the location is likely
	 * to be read-only. The client calling this method is responsible for handling the possibility that the
	 * directory may not exist.
	 * 
	 * @param subfolder
	 * @return
	 */
	public static File getStartupDir(String subfolder) {
		File result= getStartupDir();
		if (result!=null) {
			result = new File(result,subfolder);
		}
		return result;
	}

	/**
	 * Get a subdirectory of the user's application directory.
	 * <p>
	 * Like getUserDir(), but one level deeper. For instance, getUserDir("conf")
	 * on UNIX would return the file of
	 * <code>/user/myusername/.myapplication/conf</code> and assure that both
	 * <code>.myapplication</code> and <code>conf</code> are created.
	 * 
	 * @see getUserDir()
	 * @param subDirectory
	 * @return
	 */
	public static File getUserDir(String subDirectory) {
		File dir = new File(getUserDir(), subDirectory);
		dir.mkdirs();
		if (! dir.isDirectory()) {
			logger.warn("Could not create directory " + dir);
			return null;
		}
		return dir;
	}

	@Override
	protected void errorLog(String message, Throwable exception) {
		logger.error(message,exception);
	}
	
	
}
