package net.sf.taverna.tools;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.taverna.tools.ProfileListSelector.ProfileDef;

public class ProfileListSelectorTest extends TestCase {

	public void testReadingOfProfile() throws Exception {
		URL url = new URL(ProfileSelectorTest.PROFILE_BASE_URL+"test-profilelist.xml");
		ProfileListSelector list = new ProfileListSelector(url);
		
		assertEquals(9,list.getProfileList().size());
		
		assertEquals("1.5.0.0",list.getProfileList().get(0).version);
		assertEquals(ProfileSelectorTest.PROFILE_BASE_URL+"taverna-1.5.0.0-profile.xml",list.getProfileList().get(0).location);
		
		ProfileDef last = list.getProfileList().get(list.getProfileList().size()-1);
		assertEquals("1.5.1.6",last.version);
		assertEquals(ProfileSelectorTest.PROFILE_BASE_URL+"taverna-1.5.1.6-profile.xml",last.location);
	}
	
	public void testStoreFirst() throws Exception {
		URL url = new URL(ProfileSelectorTest.PROFILE_BASE_URL+"test-profilelist.xml");
		ProfileListSelector list = new ProfileListSelector(url);
		
		File tmpFile = File.createTempFile("current-profile", "xml");
		list.storeFirst(tmpFile);
		
		assertTrue(tmpFile.exists());
		
		tmpFile.delete();
	}
}
