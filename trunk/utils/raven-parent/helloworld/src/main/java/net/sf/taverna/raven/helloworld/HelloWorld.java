package net.sf.taverna.raven.helloworld;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;

public class HelloWorld {

	protected static final String TEST_DATA = "This is the test data.\n";

	public static void main(String[] args) throws IOException {
		HelloWorld helloWorld = new HelloWorld();
		helloWorld.run(System.out);
	}

	public void run(PrintStream out) throws IOException {
		File tmpFile = File.createTempFile("helloworld", "test");
		tmpFile.deleteOnExit();
		FileUtils.writeStringToFile(tmpFile, TEST_DATA, "utf8");
		String read = FileUtils.readFileToString(tmpFile, "utf8");
		out.println(read);
	}

}
