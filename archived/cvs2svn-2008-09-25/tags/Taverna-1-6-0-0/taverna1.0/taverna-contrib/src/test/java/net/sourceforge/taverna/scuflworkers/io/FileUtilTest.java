/**
 * 
 */
package net.sourceforge.taverna.scuflworkers.io;

import net.sourceforge.taverna.io.FileNameUtil;
import junit.framework.TestCase;

/**
 * @author Mark
 *
 */
public class FileUtilTest extends TestCase {

	public void testGetExtension() {
		String result = FileNameUtil.getExtension("test.xml");
		assertTrue("The extension was wrong: " + result, result.equals("xml"));
	}

	public void testGetExtensionFromPath() {
		String result = FileNameUtil.getExtension("C:\\whatever\\test.xml");
		assertTrue("The extension was wrong: " + result, result.equals("xml"));
	}

	public void testReplaceExtension() {
		String result = FileNameUtil.replaceExtension("test.xml","html");
		assertTrue("The extension was not replaced properly: " + result,result.equals("test.html"));
	}

	public void testReplacePathExtension() {
		String result = FileNameUtil.replaceExtension("something.xml","html");
		assertTrue("The result was wrong: " + result,result.equals("something.html"));
	}

}
