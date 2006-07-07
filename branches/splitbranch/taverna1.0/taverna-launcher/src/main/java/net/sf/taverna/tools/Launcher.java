package net.sf.taverna.tools;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

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
		File file = Launcher.getBootstrapFile();
		if (file.isDirectory())
			return file;
		return file.getParentFile();
	}

    // Copied from org.apache.commons.launcher.Launcher which is under
    // Apache License 2.0. 
    //
    // From http://www.apache.org/foundation/licence-FAQ.html#GPL :
    // 
	//   Is the Apache license compatible with the GPL (GNU Public License)?
    //   
	//   It is the unofficial position of The Apache Software Foundation that the
	//   Apache license is compatible with the GPL. However, the Free Software
	//   Foundation holds a different position, although we have not been able to
	//   get them to give us categorical answers to our queries asking for details
	//   on just what aspects they consider incompatible.
    //   
	//   Whether to mix software covered under these two different licenses must
	//   be a determination made by those attempting such a synthesis.
    //
	// As we are using lots of Apache licenced stuff anyway I guess this should
	// be OK.
    
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
				+ Launcher.class.getName().replace('.', '/') + ".class";
		URL resource = Launcher.class.getResource(classResourceName);
		if (resource == null)
			throw new IOException("Bootstrap file not found: "
					+ Launcher.class.getName());
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
					+ Launcher.class.getName());

		// Coerce the URL into a file and check that it exists. Note that
		// the JVM <code>File(String)</code> constructor automatically
		// flips all '/' characters to '\' on Windows and there are no
		// valid escape characters so we sould not have to worry about
		// URL encoded slashes.
		File file = new File(resourcePath);
		if (!file.exists() || !file.canRead())
			throw new IOException("Bootstrap file not found: "
					+ Launcher.class.getName());
		bootstrapFile = file.getCanonicalFile();

		return bootstrapFile;
    }

	
	
	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, MalformedURLException  {
		if (System.getProperty("taverna.home") == null) {
			
			File bootDir;
			try {
				bootDir = getBootstrapDir().getParentFile();
			} catch (IOException e) { 
				System.err.println("Can't find taverna home directory, " + e.getMessage());				
				System.exit(100);				
				return;
			}
			System.setProperty("taverna.home", bootDir.toString());
		}			
		
		if (System.getProperty("taverna.dotlocation") == null
				&& System.getProperty("os.name").toLowerCase().indexOf(
						"windows") > -1) {
			// We bundle the win32 dot.exe with Taverna
			File tavernaHome = new File(System.getProperty("taverna.home"));
			File dotLocation = new File(tavernaHome, "bin\\win32i386\\dot.exe");
			System.setProperty("taverna.dotlocation", dotLocation.toString());
		}		
		String mainClass = System.getProperty("taverna.main");
		if (mainClass == null) {
			mainClass = "org.embl.ebi.escience.scuflui.workbench.Workbench";
		}
        
		List jarURLList = new ArrayList();

		String path = System.getProperty("taverna.path");
		if (path != null) {
			String[] paths = path.split(File.pathSeparator);
			for (int i=0; i<paths.length; i++) {
				File file = new File(paths[i]);
				jarURLList.add(file.toURL());
			}
		}		
		
		File home = new File(System.getProperty("taverna.home"));
		// IMPORTANT: conf/ before anything else, to make sure say log4j.properties
		// can be overriden by users
		addJars(new File(home, "conf/"), jarURLList);
     	addJars(home, jarURLList);
		// Plugins before lib, becuse they might require newer stuff than in lib
		addJars(new File(home, "plugins/"), jarURLList);				
		addJars(new File(home, "lib/"), jarURLList);
		addJars(new File(home, "libext/"), jarURLList);				
		addJars(new File(home, "resources/"), jarURLList);
		
		URL[] urls = (URL[])jarURLList.toArray(new URL[0]);

		URLClassLoader loader = new URLClassLoader(urls, 
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(loader);
		// Preload our preferred SAX parser		
		loader.loadClass("org.apache.xerces.parsers.SAXParser");

		// Find and execute the "real" main()
		Class workbenchClass = loader.loadClass(mainClass);		
		Method mainMethod = workbenchClass.getDeclaredMethod("main",
			new Class[] { args.getClass() });
		mainMethod.invoke(null, new Object[] { args });
		
	}

	static void addJars(File dir, List populate)
			throws MalformedURLException {
		if (dir.exists()) {
			File[] jars = dir.listFiles(new FilenameFilter() {
				public boolean accept(File file, String str) {
					return (str.endsWith(".jar"));
				}
			});
			populate.add(dir.toURL());
			if (jars != null) {
				for (int i = 0; i < jars.length; i++) {
					populate.add(jars[i].toURL());
				}
			}
		}
	}

}
