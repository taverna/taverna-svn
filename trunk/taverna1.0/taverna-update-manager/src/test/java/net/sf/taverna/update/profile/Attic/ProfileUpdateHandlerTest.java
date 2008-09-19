package net.sf.taverna.update.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.tools.ProfileSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileUpdateHandlerTest {
	
	public static final String PROFILE_BASE_URL = "http://www.mygrid.org.uk/taverna-tests/testprofiles/";
	private static final String PROFILE_LIST=PROFILE_BASE_URL+"taverna-1.5.2-profiles.xml";

	File dir;
	File confdir;
	
	@Before
	public void setUp() throws Exception {
		dir = createTempDirectory().getAbsoluteFile();
		confdir=new File(dir,"conf");
		confdir.mkdir();
		
		System.setProperty("taverna.home", dir.getAbsolutePath());
		System.setProperty("taverna.startup", dir.getAbsolutePath());
		
		
	}
	
	@After
	public void tearDown() throws Exception {
		System.clearProperty("taverna.startup");
		System.clearProperty("taverna.home");
		System.clearProperty("raven.profile");
		System.clearProperty("raven.profilelist");
		
		File currentProfile = new File(confdir,ProfileSelector.CURRENT_PROFILE);
		if (currentProfile.exists()) currentProfile.delete();
		
		confdir.delete();
		dir.delete();
	}
	
	private File createTempDirectory() throws IOException {
		File tempFile;
		try {
			tempFile = File.createTempFile("tavernahome", "");
			// But we want a directory!
		} catch (IOException e) {
			System.err.println("Could not create temporary directory");
			throw e;
		}
		tempFile.delete();
		assert tempFile.mkdir();
		return tempFile;
	}
	
	@Ignore("Integration test")
	@Test
	public void testNoUpdateAvailable() throws Exception {
		URL local = copyToCurrent(new URL(PROFILE_BASE_URL+"taverna-1.5.2.1-SNAPSHOT-profile.xml"));
		ProfileUpdateHandler handler = new ProfileUpdateHandler(new URL(PROFILE_LIST),local);
		assertFalse("There should be no update available",handler.isNewVersionAvailable());
	}
	
	@Ignore("Integration test")
	@Test
	public void testUpdateAvailable() throws Exception {
		URL local = copyToCurrent(new URL(PROFILE_BASE_URL+"taverna-1.5.2.0-SNAPSHOT-profile.xml"));
		ProfileUpdateHandler handler = new ProfileUpdateHandler(new URL(PROFILE_LIST),local);
		assertTrue("There should be an update available",handler.isNewVersionAvailable());
	}

	@Ignore("Integration test")
		@Test

	public void testDoUpdate() throws Exception {
		URL local = copyToCurrent(new URL(PROFILE_BASE_URL+"taverna-1.5.2-SNAPSHOT-profile.xml"));
		ProfileUpdateHandler handler = new ProfileUpdateHandler(new URL(PROFILE_LIST),local);
		assertTrue("There should be an update available",handler.isNewVersionAvailable());
		
		handler.updateLocalProfile(new File(local.toURI()));
		
		Profile profile = new Profile(local.openStream(),true);
		
		assertEquals("1.5.2.1-SNAPSHOT",profile.getVersion());
		
		File file = new File(confdir,ProfileSelector.CURRENT_PROFILE+"-1.5.2.0-SNAPSHOT.bak");
		assertTrue("No backup file found",file.exists());
	}
	
	private URL copyToCurrent(URL remoteProfile) throws Exception {
		File destinationFile = new File(confdir,ProfileSelector.CURRENT_PROFILE);
		
		InputStream in = remoteProfile.openStream();
		
		if (!destinationFile.exists()) destinationFile.createNewFile();
		OutputStream out = new FileOutputStream(destinationFile);
		
		byte [] buffer = new byte[255];
		int len;
		while ((len = in.read(buffer))!=-1) {
			out.write(buffer,0,len);
		}
		
		in.close();
		out.close();
		
		return destinationFile.toURI().toURL();
	}
	
}
