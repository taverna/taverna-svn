package org.embl.ebi.escience.scuflui.workbench;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Bootstrap {
	
	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		File dir = new File("e:/home/tom/taverna");
		dir.mkdirs();
		// Create a remote classloader referencing the raven jar within a repository
		String repositoryLocation = 
			"http://www.ebi.ac.uk/~tmo/repository/";
		String artifactLocation = 
			"uk/org/mygrid/taverna/raven/raven/1.5-SNAPSHOT/raven-1.5-SNAPSHOT.jar";
		ClassLoader c = new URLClassLoader(
				new URL[]{new URL(repositoryLocation+artifactLocation)}
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
		String ravenVersion = "1.5-SNAPSHOT";
		URL[] remoteRepositories = new URL[]{
				new URL("http://www.ebi.ac.uk/~tmo/repository/"), 
				new URL("http://www.ibiblio.org/maven2/")};
		URL splashScreenImage = new URL("http://www.ebi.ac.uk/~tmo/mygrid/splashscreen.png");
		String groupID = "uk.org.mygrid.taverna";
		String artifactID = "taverna-workbench";
		String version = "1.5-SNAPSHOT";
		int minimumDisplayTime = 10 * 1000; // Ten seconds
		String targetClassName = "org.embl.ebi.escience.scuflui.workbench.Workbench";
		
		// Construct array for dynamic invocation
		//fixme: this will need changing when a mvn deploy is run
		
		// Call method via reflection, 'null' target as this is a static method
		Class workbenchClass = (Class)m.invoke(
				null,
				ravenVersion,
				dir,
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
}
