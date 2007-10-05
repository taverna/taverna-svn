package net.sf.taverna.update.plugin;

import java.net.URL;

import junit.framework.TestCase;

public class TestPluginSite extends TestCase {

	public void testEquals() throws Exception {
		PluginSite siteA=new PluginSite("bob",new URL("http://bob.org"));
		PluginSite siteB=new PluginSite("bob",new URL("http://bob.org"));
		PluginSite siteC=new PluginSite("bob",new URL("http://bob2.org"));
		PluginSite siteD=new PluginSite("bob2",new URL("http://bob.org"));
		
		assertEquals("they should be equal",siteA,siteB);
		assertEquals("they should be equal",siteB,siteA);
		
		assertEquals("they should have equal hashcodes",siteB.hashCode(),siteA.hashCode());
		
		assertFalse("they should not be equal",siteA.equals(siteC));
		assertFalse("they should not be equal",siteA.equals(siteD));
		assertFalse("they should not be equal",siteB.equals(siteC));
		assertFalse("they should not be equal",siteB.equals(siteD));
		assertFalse("they should not be equal",siteC.equals(siteD));
		
		assertFalse("they should not be equal",siteC.equals(siteA));
		assertFalse("they should not be equal",siteD.equals(siteA));
		assertFalse("they should not be equal",siteC.equals(siteB));
		assertFalse("they should not be equal",siteD.equals(siteB));
		assertFalse("they should not be equal",siteD.equals(siteC));
		
	}
}
