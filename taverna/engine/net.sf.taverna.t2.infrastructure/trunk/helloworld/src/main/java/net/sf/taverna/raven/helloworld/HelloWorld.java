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
