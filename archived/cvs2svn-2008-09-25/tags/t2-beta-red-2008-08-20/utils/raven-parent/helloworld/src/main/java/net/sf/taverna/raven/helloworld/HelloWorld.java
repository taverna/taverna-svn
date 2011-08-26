package net.sf.taverna.raven.helloworld;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.sf.taverna.raven.launcher.Launchable;

import org.apache.commons.io.FileUtils;

public class HelloWorld implements Launchable {

	protected static final String TEST_DATA = "This is the test data.\n";

	public static void main(String[] args) throws IOException {
		HelloWorld helloWorld = new HelloWorld();
		helloWorld.launch(args);
	}

	public void run(PrintStream out) throws IOException {
		File tmpFile = File.createTempFile("helloworld", "test");
		tmpFile.deleteOnExit();
		FileUtils.writeStringToFile(tmpFile, TEST_DATA, "utf8");
		String read = FileUtils.readFileToString(tmpFile, "utf8");
		out.print(read);
	}

	public int launch(String[] args) throws IOException {
		if (args.length == 0) {
			run(System.out);
		} else {
			PrintStream outStream = new PrintStream(args[0]);
			try {
				run(outStream);
			} finally {
				outStream.close();
			}
		}
		return 0;
	}

}
