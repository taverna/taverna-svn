/**
 * 
 */
package net.sf.taverna.raven.plugins;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class TestPluginVersionComparison {

	@Test
	public void testVersionEquality() {
		assertTrue(Plugin.compareVersionString("1.1.1", "1.1.1") == 0);
	}

	@Test
	public void testVersionInequality() {
		assertFalse(Plugin.compareVersionString("1.1.1", "1.1.2") == 0);
	}
	
	@Test
	public void testVersionPartPrecedence() {
		assertTrue(Plugin.compareVersionString("2.1.1", "1.1.2") > 0);
	}
	
	@Test
	public void testVersionSimpleOrder() {
		assertTrue(Plugin.compareVersionString("9.1.1", "8.1.1") > 0);
	}
	
	@Test
	public void testVersionNonalphabeticOrder() {
		assertTrue(Plugin.compareVersionString("10.1.1", "9.1.1") > 0);
	}
	
	@Test
	public void testVersionNonalphabeticInversion() {
		assertTrue(Plugin.compareVersionString("9.1.1", "10.1.1") < 0);
	}
	
	@Test
	public void testVersionCheckSnapshot() {
		assertTrue(Plugin.compareVersionString("2.5.0", "2.5.0-SNAPSHOT") > 0);
		assertTrue(Plugin.compareVersionString("2.5.0-SNAPSHOT", "2.5.0-SNAPSHOT") == 0);
		assertTrue(Plugin.compareVersionString("2.5-SNAPSHOT", "2.5.0") < 0);
		
		// This is strange but expected
		assertTrue(Plugin.compareVersionString("2.5-SNAPSHOT", "2.5.0-SNAPSHOT") < 0);
		
		assertTrue(Plugin.compareVersionString("2.5-SNAPSHOT", "2.5-SNAPSHOT-20140204") < 0);
		assertTrue(Plugin.compareVersionString("2.5.SNAPSHOT", "2.5-SNAPSHOT-20140204") < 0);
	}
	
	@Test
	public void testVersionPartPadding() {
		assertTrue(Plugin.compareVersionString("2.5", "2.5.0") == 0);
	}

}
