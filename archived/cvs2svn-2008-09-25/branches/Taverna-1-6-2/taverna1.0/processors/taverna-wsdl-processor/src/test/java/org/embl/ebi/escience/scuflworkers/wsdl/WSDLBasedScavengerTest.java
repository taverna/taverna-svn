package org.embl.ebi.escience.scuflworkers.wsdl;

import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;

public class WSDLBasedScavengerTest extends WSDLBasedTestCase {

	public void testScavenger() throws Exception {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				TESTWSDL_BASE+"GUIDGenerator.wsdl");
		assertEquals(
				"root description is incorrect",
				"porttype: OTNGUIDGeneratorPortType [<font color=\"green\">RPC</font>]",
				scavenger.getFirstChild().toString());
	}

	public void testScavengerNCBIEutils_Lite() throws Exception {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				TESTWSDL_BASE+"eutils/eutils_lite.wsdl");
		assertEquals(
				"root description is incorrect",
				"porttype: eUtilsServiceSoap [<font color=\"blue\">DOCUMENT</font>]",
				scavenger.getFirstChild().toString());
	}

}
