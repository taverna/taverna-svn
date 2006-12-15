package net.sf.taverna.raven.repository;

import junit.framework.TestCase;

public class BasicArtifactTest extends TestCase {

	public void testToString() {
		BasicArtifact mavenReporting = new BasicArtifact("org.apache.maven.reporting",
				"maven-reporting-api","2.0");
		assertEquals("org.apache.maven.reporting:maven-reporting-api:2.0", 
				mavenReporting.toString());
	}
	
	public void testEqual() {
		BasicArtifact batik = new BasicArtifact("batik","batik-swing","1.6");
		BasicArtifact batikSame = new BasicArtifact("batik","batik-swing","1.6");
		BasicArtifact batikVersion = new BasicArtifact("batik","batik-swing","1.6.1");
		BasicArtifact batikArtifact = new BasicArtifact("batik","batik-swings","1.6");
		BasicArtifact batikGroup = new BasicArtifact("batikk","batik-swing","1.6");
		assertEquals(batik, batik);
		assertEquals(batik, batikSame);
		assertFalse(batik.equals(batikVersion));
		assertFalse(batik.equals(batikArtifact));
		assertFalse(batik.equals(batikGroup));
		
		assertEquals(0, batik.compareTo(batikSame));		
		assertTrue(batik.compareTo(batikVersion) < 0);		
		assertTrue(batik.compareTo(batikArtifact) < 0);		
		assertTrue(batik.compareTo(batikGroup) < 0);		
		assertTrue(batikVersion.compareTo(batikGroup) < 0);		

		
	}
	
	
}
