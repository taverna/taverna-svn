package org.embl.ebi.escience.scuflworkers.wsdl;

import junit.framework.TestCase;

public class WSDLBasedScavengerTest extends TestCase {

	public void testScavenger() throws Exception {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				"http://webservices.oracle.com/ws/guid/oracle.ws.OTNGUIDGenerator?WSDL");
		assertEquals(
				"root description is incorrect",
				"porttype: OTNGUIDGeneratorPortType [<font color=\"green\">RPC</font>]",
				scavenger.getFirstChild().toString());
	}

	public void testScavengerNCBIEutils_Lite() throws Exception {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl");
		assertEquals(
				"root description is incorrect",
				"porttype: eUtilsServiceSoap [<font color=\"blue\">DOCUMENT</font>]",
				scavenger.getFirstChild().toString());
	}

}
