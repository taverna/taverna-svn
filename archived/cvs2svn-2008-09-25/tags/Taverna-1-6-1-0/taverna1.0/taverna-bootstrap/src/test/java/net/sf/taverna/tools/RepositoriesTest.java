package net.sf.taverna.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

public class RepositoriesTest extends TestCase {
	
	File dir;
	File repositoryDir;

	@Override
	protected void setUp() throws Exception {
		
		String resourcePath = RavenPropertiesTest.class.getResource("/conf/raven.properties").toExternalForm();
		resourcePath=resourcePath.replaceAll("file:","");
		resourcePath=resourcePath.replaceAll("conf/raven.properties", "");
		System.out.println("Looking for conf/raven.properties in: "+resourcePath);
		System.setProperty("taverna.home", resourcePath);
		
		dir = createTempDirectory().getAbsoluteFile();
		repositoryDir=new File(dir,"repository");
		
		System.setProperty("taverna.startup", dir.getAbsolutePath());	
	}
	
	@Override
	protected void tearDown() throws Exception {
		System.clearProperty("taverna.startup");
		System.clearProperty("taverna.home");
		System.clearProperty("raven.profile");
		System.clearProperty("raven.profilelist");
		
		
		if (repositoryDir.exists()) repositoryDir.delete();
		dir.delete();
		
		RavenProperties.getInstance().flush();
	}
	
	private File createTempDirectory() throws IOException {
		File tempFile;
		try {
			tempFile = File.createTempFile("tavernastartup", "");
			// But we want a directory!
		} catch (IOException e) {
			System.err.println("Could not create temporary directory");
			throw e;
		}
		tempFile.delete();
		assert tempFile.mkdir();
		return tempFile;
	}

	public void testStartupRepositoryNotDefined() throws Exception {
		System.clearProperty("taverna.startup");
		URL[] urls = new Repositories().find();
		
		//check its not present if not defined
		for (URL url : urls) {
			if (url.equals(repositoryDir.toURL())) {
				fail("repository URL should not have been added");
			}
		}
	}
	
	public void testStartupRepositoryDefinedButNotExist() throws Exception {
		System.setProperty("taverna.startup", dir.getAbsolutePath());
		URL[] urls = new Repositories().find();
		
		for (URL url : urls) {
			if (url.equals(repositoryDir.toURL())) {
				fail("repository URL should not have been added");
			}
		}
	}
	
	public void testStartupRepositoryDefinedAndExists() throws Exception {
		System.setProperty("taverna.startup", dir.getAbsolutePath());
		boolean createdStatus = repositoryDir.mkdir();
		assertTrue("Unable to create repository directory",createdStatus);
		URL[] urls = new Repositories().find();
		
		boolean found = false;
		for (URL url : urls) {
			if (url.equals(repositoryDir.toURL())) {
				found = true;
			}
		}
		
		assertTrue("repository in startup was not added to repositories",found);
		assertEquals("startup repository should be first entry in list",repositoryDir.toURL(),urls[0]);
	}
}
