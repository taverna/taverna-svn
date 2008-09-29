package org.embl.ebi.escience.lsidauth;

import junit.framework.TestCase;

/**
 * Test that the library can be initialized
 * @author Tom Oinn
 */
public class InitTest extends TestCase {
    
    public void testInit() {
	byte[] sampleKey = "asdkjhsdkjfhsdf".getBytes();
	try {
	    LSIDAuth.init(sampleKey);
	}
	catch (Exception ex) {
	    fail(ex.getMessage());
	}
	assertTrue("Instance creation failed", LSIDAuth.getInstance()!=null);
    }

}
