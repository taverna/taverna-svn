/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
