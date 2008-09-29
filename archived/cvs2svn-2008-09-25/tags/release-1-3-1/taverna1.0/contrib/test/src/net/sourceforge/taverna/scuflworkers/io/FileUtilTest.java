/**
 * 
 */
package net.sourceforge.taverna.scuflworkers.io;

import net.sourceforge.taverna.io.FileUtil;
import junit.framework.TestCase;

/**
 * @author Mark
 *
 */
public class FileUtilTest extends TestCase {

	public void testGetExtension() {
		String result = FileUtil.getExtension("test.xml");
		assertTrue("The extension was wrong: " + result, result.equals("xml"));
	}

	public void testGetExtensionFromPath() {
		String result = FileUtil.getExtension("C:\\whatever\\test.xml");
		assertTrue("The extension was wrong: " + result, result.equals("xml"));
	}

	public void testReplaceExtension() {
		String result = FileUtil.replaceExtension("test.xml","html");
		assertTrue("The extension was not replaced properly: " + result,result.equals("test.html"));
	}

	public void testReplacePathExtension() {
		String result = FileUtil.replaceExtension("something.xml","html");
		assertTrue("The result was wrong: " + result,result.equals("something.html"));
	}

}
