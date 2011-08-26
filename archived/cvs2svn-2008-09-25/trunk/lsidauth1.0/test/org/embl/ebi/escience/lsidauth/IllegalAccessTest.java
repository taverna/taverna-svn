package org.embl.ebi.escience.lsidauth;

import junit.framework.TestCase;

/**
 * Test that attempting to use an expired instance of the
 * LSIDAuth causes an exception.
 * @author Tom Oinn
 */
public class IllegalAccessTest extends TestCase {
    
    public void testIllegalAccess() {	
	try {
	    LSIDAuth.getInstance();
	    fail("Should not be able to obtain unitialised instance.");
	}
	catch (Exception ex) {
	    //
	}
	byte[] sampleKey1 = "asdkjhsdkjfhsdf".getBytes();
	byte[] sampleKey2 = "asdkjhsdkjfhsdf".getBytes();
	LSIDAuth.init(sampleKey1);
	LSIDAuth oldAuth = LSIDAuth.getInstance();
	LSIDAuth.init(sampleKey2);
	try {
	    String cryptedPassword = oldAuth.createPassword("SecretPassword");
	    fail("Should have caused an exception from reuse of invalid instance.");
	}
	catch (Exception ex) {
	    //
	}
    }

}
