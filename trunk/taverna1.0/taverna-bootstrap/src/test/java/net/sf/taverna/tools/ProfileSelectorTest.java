package net.sf.taverna.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileSelectorTest {

	File dir;
	File confdir;

	public static final String PROFILE_BASE_URL = "http://www.mygrid.org.uk/taverna-tests/testprofiles/";
	private Properties properties;

	@Before
	public void setUp() throws Exception {

		properties = new Properties();
		dir = createTempDirectory().getAbsoluteFile();
		confdir = new File(dir, "conf");
		confdir.mkdir();

		System.setProperty("taverna.home", dir.getAbsolutePath());
		System.setProperty("taverna.startup", dir.getAbsolutePath());

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

	@After
	public void tearDown() throws Exception {
		System.clearProperty("taverna.startup");
		System.clearProperty("taverna.home");
		System.clearProperty("raven.profile");
		System.clearProperty("raven.profilelist");

		File currentProfile = new File(confdir, ProfileSelector.CURRENT_PROFILE);
		if (currentProfile.exists())
			currentProfile.delete();

		File dummyProfile = new File(confdir, "profile.xml");
		if (dummyProfile.exists())
			dummyProfile.delete();

		confdir.delete();
		dir.delete();
	}

	@Test
	public void testExplicitProfileDefined() {
		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");
		properties.setProperty("raven.profile", PROFILE_BASE_URL
				+ "test-profile.xml");
		ProfileSelector selector = new ProfileSelector(properties);

		assertEquals(PROFILE_BASE_URL + "test-profile.xml", selector
				.getProfileLocation());

		assertEquals(PROFILE_BASE_URL + "test-profile.xml", properties
				.getProperty("raven.profile"));

		assertNull(
				"raven.profilelist should be removed if using a forced profile",
				properties.getProperty("raven.profilelist"));
	}

	@Test
	@Ignore("Integration test")
	public void testUseCurrentIfAvaliableWhenListDefined() throws Exception {
		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");

		File current = new File(confdir, ProfileSelector.CURRENT_PROFILE);
		current.createNewFile();

		ProfileSelector selector = new ProfileSelector(properties);

		String expected = "file:" + confdir.getAbsolutePath()
				+ File.separatorChar + ProfileSelector.CURRENT_PROFILE;

		assertEquals(expected, selector.getProfileLocation());
		assertEquals(expected, properties.getProperty("raven.profile"));
		assertNotNull("raven.profilelist has disappeared", properties
				.getProperty("raven.profilelist"));
	}

	@Test
	@Ignore("Integration test")
	public void testProfileList() {
		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");
		ProfileSelector selector = new ProfileSelector(properties);

		String expected = "file:" + confdir.getAbsolutePath()
				+ File.separatorChar + ProfileSelector.CURRENT_PROFILE;

		assertEquals(expected, selector.getProfileLocation());
		assertEquals(expected, properties.getProperty("raven.profile"));
		assertNotNull("raven.profilelist property has disappeared", properties
				.getProperty("raven.profilelist"));
	}

	/**
	 * check that current-profile-1.5.2.xml is not overwritten if it already
	 */
	@Test
	@Ignore("Integration test")
	public void testCurrentNotOverwritten() throws Exception {
		File current = new File(confdir, "current-profile-1.5.2.xml");
		current.createNewFile();

		Writer writer = new PrintWriter(current);
		writer.write("monkey");
		writer.close();

		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");
		ProfileSelector selector = new ProfileSelector(properties);

		String expected = "file:" + confdir.getAbsolutePath()
				+ File.separatorChar + ProfileSelector.CURRENT_PROFILE;

		assertEquals(expected, selector.getProfileLocation());
		assertEquals(expected, properties.getProperty("raven.profile"));

		Reader reader = new FileReader(current);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = bufferedReader.readLine();
		reader.close();
		assertEquals("contents of file should not have changed", "monkey", line);
		assertNotNull("raven.profilelist property has disappeared", properties
				.getProperty("raven.profilelist"));
	}

	@Test
	@Ignore("Integration test")
	public void testProfileInStartupConf() throws Exception {
		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");
		File dummyProfile = new File(confdir, "profile.xml");
		dummyProfile.createNewFile();

		ProfileSelector selector = new ProfileSelector(properties);

		String expected = "file:" + dummyProfile.getAbsolutePath();

		assertEquals(expected, selector.getProfileLocation());
		assertEquals(expected, properties.getProperty("raven.profile"));
		assertNull(
				"raven.profilelist should be removed if using profile in the startup/conf/profile.xml",
				properties.getProperty("raven.profilelist"));
	}

	@Test
	@Ignore("Integration test")
	public void testProfileOverridesList() throws Exception {
		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");
		properties.setProperty("raven.profile", PROFILE_BASE_URL
				+ "test-profile.xml");
		ProfileSelector selector = new ProfileSelector(properties);

		assertEquals(PROFILE_BASE_URL + "test-profile.xml", selector
				.getProfileLocation());

		assertEquals(PROFILE_BASE_URL + "test-profile.xml", properties
				.getProperty("raven.profile"));
		assertNull(
				"raven.profilelist should be removed if using profile in the startup/conf/profile.xml",
				properties.getProperty("raven.profilelist"));
	}

	@Test
	@Ignore("Integration test")
	public void testUseCurrentWhenNotDefined() throws Exception {
		properties.setProperty("raven.profilelist", PROFILE_BASE_URL
				+ "test-profilelist.xml");
		File current = new File(confdir, ProfileSelector.CURRENT_PROFILE);
		current.createNewFile();

		ProfileSelector selector = new ProfileSelector(properties);

		String expected = "file:" + current.getAbsolutePath();

		assertEquals(expected, selector.getProfileLocation());
		assertEquals(expected, properties.getProperty("raven.profile"));
		assertNotNull("raven.profilelist has disappeared", properties
				.getProperty("raven.profilelist"));
	}

	/**
	 * test for space separated list of URLs
	 */
	@Test
	@Ignore("Integration test")
	public void testProfileMirrorList() {
		properties.setProperty("raven.profile",
				"http://35E62FF5-324C-4C1B-AB24-4FF6BE7D1C0E.not/profile.xml "
						+ PROFILE_BASE_URL + "taverna-1.5.0.0-profile.xml");
		ProfileSelector selector = new ProfileSelector(properties);
		assertEquals("list should have been resolved down to 1 url",
				PROFILE_BASE_URL + "taverna-1.5.0.0-profile.xml", selector
						.getProfileLocation());
		assertEquals("list should have been resolved down to 1 url",
				PROFILE_BASE_URL + "taverna-1.5.0.0-profile.xml", properties
						.getProperty("raven.profile"));
	}

	@Test
	@Ignore("Integration test")
	public void testProfileListMirrorList() {
		properties.setProperty("raven.profilelist",
				"http://35E62FF5-324C-4C1B-AB24-4FF6BE7D1C0E.not/profile.xml "
						+ PROFILE_BASE_URL + "test-profilelist.xml");
		new ProfileSelector(properties);
		assertEquals("list should have been resolved down to 1 url",
				PROFILE_BASE_URL + "test-profilelist.xml", properties
						.getProperty("raven.profilelist"));
	}

}
