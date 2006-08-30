package net.sf.taverna.raven;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.impl.ArtifactImpl;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import junit.framework.TestCase;

public class LoaderTest extends TestCase {

	File dir;
	
	public void setUp() {
		dir = createTempDirectory();
		//dir = new File("C:\\testRaven");
	}


	public void tearDown() {
		try {
			deleteDirectory(dir);
		} catch (IOException e) {
			//
		}
	}

	
	private static File createTempDirectory() {
		File tempFile;
		try {
			tempFile = File.createTempFile("raven", "");
			// But we want a directory!
		} catch (IOException e) {
			System.err.println("Could not create temporary directory");
			e.printStackTrace();
			return null;
		}
		tempFile.delete();
		assert tempFile.mkdir();
		return tempFile;
	}
		
	/**
	 * Test dynamic (reflection based) access to a remote raven repository
	 * without using any reference to the raven API
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InterruptedException 
	 */
	public void testDynamic() throws MalformedURLException, ClassNotFoundException, 
	SecurityException, NoSuchMethodException, IllegalArgumentException, 
	IllegalAccessException, InvocationTargetException, InterruptedException {
		
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
				"doRavenMagic",String.class,File.class,
				URL[].class,URL.class,String.class,
				String.class,String.class,int.class,String.class);
		
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
		Object[] args = new Object[]{
				ravenVersion,
				dir,
				remoteRepositories,
				splashScreenImage,
				groupID,
				artifactID,
				version,
				minimumDisplayTime,
				targetClassName
		};
		
		// Call method via reflection, 'null' target as this is a static method
		Class workbenchClass = (Class)m.invoke(null,args);
		
		// Verify that the class is loaded and that the classloader is being
		// driven off Raven's artifact system
		assertTrue(workbenchClass.getName().equals("org.embl.ebi.escience.scuflui.workbench.Workbench"));
		assertTrue(workbenchClass.getClassLoader().toString().equals("loader{uk.org.mygrid.taverna:taverna-workbench:1.5-SNAPSHOT}"));
		
		// Could invoke the workbench's main method here as follows :
		//
		// Method workbenchMain = workbenchClass.getDeclaredMethod("main",String[].class);
		// workbenchMain.invoke(null,new Object[]{new String[0]});
	}
	
	/**
	 * Test whether we can launch the workbench from a local raven jar
	 * @throws MalformedURLException
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 */
	public void testNonDynamic() throws MalformedURLException, ArtifactNotFoundException, ArtifactStateException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager
				.getSystemLookAndFeelClassName());
		Class workbenchClass = Loader.doRavenMagic(
				"1.5-SNAPSHOT",
				dir,
				new URL[]{new URL("http://www.ebi.ac.uk/~tmo/repository/"), 
				          new URL("http://www.ibiblio.org/maven2/")},
				new URL("http://www.ebi.ac.uk/~tmo/mygrid/splashscreen.png"),
				"uk.org.mygrid.taverna",
				"taverna-workbench",
				"1.5-SNAPSHOT",
				10000,
		"org.embl.ebi.escience.scuflui.workbench.Workbench");
		System.out.println(workbenchClass.toString());
		System.out.println(workbenchClass.getClassLoader().toString());
		/**
		System.out.println("\n\nRepository state dump : \n");
		LocalRepository r = LocalRepository.getRepository(dir);
		for (Artifact a : r.getArtifacts()) {
			ArtifactImpl ai = (ArtifactImpl)a;
			System.out.println(ai.toString()+"  ##  "+r.getStatus(ai).toString());
			for (ArtifactImpl dep : ai.getDependencies()) {
				System.out.println("    "+dep.toString()+"  --  "+r.getStatus(dep).toString());
			}
		}
		*/
	}
	
	public void testDummy() {
		//
	}
	
	
}
