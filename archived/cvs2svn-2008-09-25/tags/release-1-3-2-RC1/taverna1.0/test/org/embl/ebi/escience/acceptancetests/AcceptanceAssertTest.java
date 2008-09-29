package org.embl.ebi.escience.acceptancetests;



import org.embl.ebi.escience.testhelpers.acceptance.*;

public class AcceptanceAssertTest extends AcceptanceTestCase
{
	
	public void testNull()
	{
		assertNull("this should be null",null);		
	}
	
	public void testNotNull()
	{
		assertNotNull("this should not be null","not null");		
	}
	
	public void testPrimitives()
	{
		assertEquals("int",2,2);
		assertEquals("double",1.567,1.567);
		assertEquals("float",1.567f,1.567f);			
	}
	
	public void testTrue()
	{
		assertTrue("this should be true",true);
	}
	
	public void testFalse()
	{
		assertFalse("this should be false",false);
	}
	
	public void testEquals()
	{
		assertEquals("these should be the same","test string","test string");
	}		
}
