package org.embl.ebi.escience.lsidauth;

import junit.framework.TestCase;

/**
 * Test that the encrypted password verifies against
 * the plaintext and that the wrong password doesn't.
 * @author Tom Oinn
 */
public class VerifyTest extends TestCase {
    
    /**
     * Test that a correct password is recognized and an incorrect
     * one rejected by the comparePasswords method
     */
    public void testMismatchedPasswords() {
	byte[] sampleKey = "asdkjhsdkjfhsdf".getBytes();
	LSIDAuth.init(sampleKey);
	LSIDAuth auth = LSIDAuth.getInstance();
	try {
	    String cryptedPassword = auth.createPassword("SecretPassword");
	    assertTrue("Passwords don't match!",
		       auth.comparePasswords(Base64.decode(cryptedPassword),
					     "SecretPassword"));
	    assertFalse("Passwords match!",
			auth.comparePasswords(Base64.decode(cryptedPassword),
					      "AnotherSecretPassword"));
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    fail(ex.getMessage());
	}
    }
    
    /**
     * Test that the correct password is rejected if a different
     * secret key is used in the initialization of the library
     */
    public void testMismatchedSecretKeys() {
	byte[] sampleKey1 = "TheCorrectKey".getBytes();
	byte[] sampleKey2 = "TheWrongKey".getBytes();
	try {
	    LSIDAuth.init(sampleKey1);
	    String cryptedPassword = LSIDAuth.getInstance().createPassword("SecretPassword");
	    assertTrue("Correct password rejected!",
		       LSIDAuth.getInstance().comparePasswords(Base64.decode(cryptedPassword),
							       "SecretPassword"));
	    LSIDAuth.init(sampleKey2);
	    assertFalse("Password still matches after new secret key!",
			LSIDAuth.getInstance().comparePasswords(Base64.decode(cryptedPassword),
								"SecretPassword"));
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    fail(ex.getMessage());
	}
    }
}
