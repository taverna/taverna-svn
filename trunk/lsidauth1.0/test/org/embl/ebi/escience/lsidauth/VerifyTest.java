package org.embl.ebi.escience.lsidauth;

import junit.framework.TestCase;

/**
 * Test that the encrypted password verifies against
 * the plaintext and that the wrong password doesn't.
 * @author Tom Oinn
 */
public class VerifyTest extends TestCase {
    
    public void testVerify() {
	byte[] sampleKey = "asdkjhsdkjfhsdf".getBytes();
	LSIDAuth.init(sampleKey);
	LSIDAuth auth = LSIDAuth.getInstance();
	try {
	    byte[] cryptedPassword = auth.createPassword("SecretPassword");
	    assertTrue("Passwords don't match!",
		       auth.comparePasswords(cryptedPassword,
					     "SecretPassword"));
	    assertFalse("Passwords match!",
			auth.comparePasswords(cryptedPassword,
					      "AnotherSecretPassword"));
	}
	catch (Exception ex) {
	    fail(ex.getMessage());
	}
    }

}
