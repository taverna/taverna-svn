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
package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import net.sf.taverna.t2.cloudone.datamanager.AbstractBlobStoreTest;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test {@link FileBlobStore} using the tests of {@link AbstractBlobStoreTest}.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class FileBlobStoreTest extends AbstractBlobStoreTest {

	private static File tmpDir;

	@AfterClass
	public static void deleteTmp() throws IOException {
//		System.out.println("FileBlobStore dir " + tmpDir);
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// OK
		}
	}

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}


	@Override
	public void setDataManagerAndBlobStore() {
		dManager = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
		blobStore = dManager.getBlobStore();
	}

}
