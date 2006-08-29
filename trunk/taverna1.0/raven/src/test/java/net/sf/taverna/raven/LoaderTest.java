package net.sf.taverna.raven;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
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
	
	public void testDummy() {
	
	}
	
	/**
	public void testWorkbenchBootstrap() throws MalformedURLException, ArtifactNotFoundException, ArtifactStateException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
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
	}
	*/

	
}
