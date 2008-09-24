package org.embl.ebi.escience.lsidauth;

import junit.framework.TestCase;

/**
 * Test that the library can create an encrypted
 * password using a secret key and a plain text
 * string.
 * @author Tom Oinn
 */
public class EncryptTest extends TestCase {
    
    public void testEncrypt() {
	byte[] sampleKey = "myGridKey".getBytes();
	LSIDAuth.init(sampleKey);
	LSIDAuth auth = LSIDAuth.getInstance();
	try {
	    System.out.println("Secret key : \"myGridKey\".getBytes()");
	    System.out.println("Password   : SecretPassword");
	    String cryptedPass = auth.createPassword("SecretPassword");
	    System.out.println("Encrypted  : "+cryptedPass);
	}
	catch (Exception ex) {
	    fail(ex.getMessage());
	}
    }

}
