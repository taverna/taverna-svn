package org.embl.ebi.escience.scuflworkers.wsdl;

import junit.framework.*;

public class WSDLBasedScavengerTest extends TestCase 
{
	public void testScavenger() throws Exception
	{		
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger("http://webservices.oracle.com/ws/guid/oracle.ws.OTNGUIDGenerator?WSDL");			
	}
}
