package net.sf.taverna.tools;



import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Bootstrap {
	
	private static String develRepo = "";
	static {
		File mavenRep = new File(System.getProperty("user.home"), ".m2/repository/");
		if (! mavenRep.isDirectory()) {
			// Use current directory instead
			mavenRep = new File(System.getProperty("user.dir"));
			assert mavenRep.isDirectory();
		}
		try {
			develRepo = mavenRep.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			System.err.println("Invalid user.home: " + mavenRep);
			assert false;
		}
	}
	// FIXME: Avoid hardcoding!
	//repository for finding raven should come first!
	public static final String [] REPOSITORY_LIST = new String[] {
		develRepo, 
		"http://www.mygrid.org.uk/maven/repository/", 
		"http://www.ibiblio.org/maven2/"
	};
	// Where Raven will store its repository, discovered by main()
	public static String TAVERNA_CACHE="";
	public static final String RAVEN_VERSION="1.5-SNAPSHOT";
	public static final String SPLASHSCREEN="http://www.ebi.ac.uk/~tmo/mygrid/splashscreen.png";
	public static final String WORKBENCH_VERSION="1.5-SNAPSHOT";
	// Application name, as in $HOME/.taverna 
	private final static String APPLICATION = "Taverna";
	
	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		findUserDir();
		String tavernaCache = System.getProperty("taverna.repository");
		File cacheDir;
		if (tavernaCache != null) {
			cacheDir = new File(tavernaCache);
		} else {
			String TAVERNA_HOME = System.getProperty("taverna.home");
			if (TAVERNA_HOME == null) {
				System.err.println("Could not locate a Taverna home / local repository");
				return;
			}
			cacheDir = new File(TAVERNA_HOME, "repository");
		}
		cacheDir.mkdirs();
		if (! cacheDir.isDirectory()) {
			System.err.println("Not a valid repository directory: " + cacheDir);
			return;
		}
		TAVERNA_CACHE = cacheDir.getAbsolutePath();
		
		// Create a remote classloader referencing the raven jar within a repository
		String repositoryLocation = REPOSITORY_LIST[0];			
		String artifactLocation = 
			"uk/org/mygrid/taverna/raven/raven/1.5-SNAPSHOT/raven-1.5-SNAPSHOT.jar";
		URL artifactURL = new URL(new URL(repositoryLocation), artifactLocation);
		//System.out.println("Loading Raven from " + artifactURL);
		ClassLoader c = new URLClassLoader(
				new URL[]{artifactURL}
				,null);
		
		// Reference to the Loader class within net.sf.taverna.raven
		Class loaderClass = c.loadClass("net.sf.taverna.raven.Loader");
		// Find the single static method provided by the loader
		Method m = loaderClass.getDeclaredMethod(
				"doRavenMagic",
				String.class,
				File.class,
				URL[].class,
				String.class,
				String.class,
				String.class,
				String.class,
				URL.class,
				int.class);
		
		// Parameters for the Raven loader call
		String ravenVersion = RAVEN_VERSION;
		URL[] remoteRepositories = new URL[REPOSITORY_LIST.length];
		for (int i=0;i<remoteRepositories.length;i++) {
			remoteRepositories[i]=new URL(REPOSITORY_LIST[i]);
		}
		
		// FIXME: Support non-splashscreen
		URL splashScreenImage = new URL(SPLASHSCREEN);
		// FIXME: Support other classes like WorkflowLauncher
		String groupID = "uk.org.mygrid.taverna";
		String artifactID = "taverna-workbench";
		String version = WORKBENCH_VERSION;
		int minimumDisplayTime = 10 * 1000; // Ten seconds
		String targetClassName = "org.embl.ebi.escience.scuflui.workbench.Workbench";
		
		// Construct array for dynamic invocation
		//fixme: this will need changing when a mvn deploy is run
		
		// Call method via reflection, 'null' target as this is a static method
		Class workbenchClass = (Class)m.invoke(
				null,
				ravenVersion,
				cacheDir,
				remoteRepositories,
				groupID,
				artifactID,
				version,
				targetClassName,
				splashScreenImage,
				minimumDisplayTime);	
		Method workbenchStatic = workbenchClass.getMethod(
				"getWorkbench",new Class[0]);
		workbenchStatic.invoke(null, new Object[0]);
	}

	/**
	 * Find and create if neccessary the user's application directory, 
	 * according to operating system standards. The resolved directory
	 * is then stored in the system property <code>taverna.home</code>
	 * <p>
	 * If the system property <code>taverna.home</code> already exists, 
	 * the directory specified by that path will be used instead and created
	 * if needed.
	 * <p>
	 * If any exception occurs (such as out of diskspace), taverna.home will be unset.
	 * 
	 * <p>
	 * On Windows, this will typically be something like:
	 * 
	 * <pre>
	 *  	C:\Document and settings\MyUsername\Application Data\MyApplication
	 * </pre>
	 * 
	 * while on Mac OS X it will be something like:
	 * 
	 * <pre>
	 *  	/Users/MyUsername/Library/Application Support/MyApplication
	 * </pre>
	 * 
	 * All other OS'es are assumed to be UNIX-alike, returning something like:
	 * 
	 * <pre>
	 *  	/user/myusername/.myapplication
	 * </pre>
	 * 
	 * <p>
	 * If the directory does not already exist, it will be created.
	 * </p>
	 * 
	 * @return System property <code>taverna.home</code> contains
	 *         path of an existing directory for Taverna user-centric
	 *         files.
	 */
	public static void findUserDir() {
		File appHome;
		String tavHome = System.getProperty("taverna.home");
		if (tavHome != null) {
			appHome = new File(tavHome);
		} else {
			File home = new File(System.getProperty("user.home"));
			if (!home.isDirectory()) {
				// logger.error("User home not a valid directory: " + home);
				return;
			}
			String os = System.getProperty("os.name");
			// logger.debug("OS is " + os);
			if (os.equals("Mac OS X")) {
				File libDir = new File(home, "Library/Application Support");
				libDir.mkdirs();
				appHome = new File(libDir, APPLICATION);
			} else if (os.startsWith("Windows")) {
				String APPDATA = System.getenv("APPDATA");
				File appData = null;
				if (APPDATA != null) {
					appData = new File(APPDATA);
				}
				if (appData != null && appData.isDirectory()) {
					appHome = new File(appData, APPLICATION);
				} else {
					// logger.warn("Could not find %APPDATA%: " + APPDATA);
					appHome = new File(home, APPLICATION);
				}
			} else {
				// We'll assume UNIX style is OK
				appHome = new File(home, "." + APPLICATION.toLowerCase());
			}
		}
		if (!appHome.exists()) {
			if (appHome.mkdir()) {
				// logger.info("Created " + appHome);
			} else {
				// logger.error("Could not create " + appHome);				
				System.clearProperty("taverna.home");
				return;
			}
		}
		if (!appHome.isDirectory()) {
			// logger.error(APPLICATION + " user home not a valid directory: "
			// + appHome);
			System.clearProperty("taverna.home");
			return;
		}
		System.setProperty("taverna.home", appHome.getAbsolutePath());
		return;
	}
}
