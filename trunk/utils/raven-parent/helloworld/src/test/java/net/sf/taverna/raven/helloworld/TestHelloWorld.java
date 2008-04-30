package net.sf.taverna.raven.helloworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TestHelloWorld {

	@Test
	public void main() throws Exception {
		HelloWorld.main(new String[0]);
	}

	@Test
	public void mainWithFilename() throws IOException {
		File tmpFile = File.createTempFile(getClass().getCanonicalName(),
				"test");
		tmpFile.deleteOnExit();
		assertTrue(tmpFile.isFile());
		String fileContent = FileUtils.readFileToString(tmpFile, "utf8");
		assertEquals("File was not empty", "", fileContent);

		HelloWorld.main(new String[] { tmpFile.getAbsolutePath() });
		fileContent = FileUtils.readFileToString(tmpFile, "utf8");
		assertEquals("File did not contain expected output",
				HelloWorld.TEST_DATA, fileContent);
	}

	@Test
	public void runWithPrinter() throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(outStream);
		new HelloWorld().run(printStream);
		printStream.close();
		String printedString = new String(outStream.toByteArray());
		assertEquals("Did not print expected output", HelloWorld.TEST_DATA,
				printedString);
	}

}
