package net.sf.taverna.tools;


import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException  {
		if (System.getProperty("taverna.home") == null) {
			System.out.println("Can't find taverna home directory, failing.");
			System.exit(100);
		}
		String mainClass = System.getProperty("taverna.main");
		if (mainClass == null) {
			mainClass = "org.embl.ebi.escience.scuflui.workbench.Workbench";
		}
        
		List<URL> jarURLList = new ArrayList<URL>();

		String path = System.getProperty("taverna.path");
		if (path != null) {
			String[] paths = path.split(File.pathSeparator);
			for (int i=0; i<paths.length; i++) {
				File file = new File(paths[i]);
				jarURLList.add(file.toURL());
			}
		}		
		
		File home = new File(System.getProperty("taverna.home"));
		addJars(home, jarURLList);
		addJars(new File(home, "lib/"), jarURLList);
		addJars(new File(home, "libext/"), jarURLList);
		addJars(new File(home, "plugins/"), jarURLList);
		addJars(new File(home, "conf/"), jarURLList);
		addJars(new File(home, "resources/"), jarURLList);
		
		URL[] urls = jarURLList.toArray(new URL[0]);

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

	static void addJars(File dir, List<URL> populate)
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
