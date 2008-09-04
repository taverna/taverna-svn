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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManagerTest;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link FileDataManager} using the tests of
 * {@link AbstractDataManagerTest} in addition to testing that generated IDs are
 * UUIDs.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileDataManagerTest extends AbstractDataManagerTest {

	private static File tmpDir;

	@AfterClass
	public static void deleteTmp() {
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ioe) {
			// Ignore, sometimes happens on windows machines,
			// see http://issues.apache.org/jira/browse/IO-17
		}
	}

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}

	private FileDataManager fileDataManager;

	FileDataManager fileDataManager2;

	@Test
	public void generateIdDataUUID() {
		String dataId = fileDataManager.generateId(IDType.Data);
		String dataId2 = fileDataManager2.generateId(IDType.Data);
		assertFalse("Not unique identifiers", dataId.equals(dataId2));

		String dataPrefix = "urn:t2data:ddoc://" + TEST_NS + "/";
		assertTrue(dataId.startsWith(dataPrefix));
		UUID uuid = UUID.fromString(dataId.replace(dataPrefix, ""));
		assertEquals(4, uuid.version()); // random
	}

	@Before
	public void makeExtraDataManager() {
		fileDataManager2 = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
	}

	@Test
	public void nextDataIdentifierUUID() {
		DataDocumentIdentifier dataId = fileDataManager.nextDataIdentifier();
		UUID uuid = UUID.fromString(dataId.getName());
		assertEquals(4, uuid.version()); // random
	}

	@Test
	public void nextErrorIdentifierUUID() {
		ErrorDocumentIdentifier errorId = fileDataManager.nextErrorIdentifier(
				2, 3);
		UUID uuid = UUID.fromString(errorId.getName());
		assertEquals(4, uuid.version()); // random
	}

	@Test
	public void nextListIdentifierUUID() {
		EntityListIdentifier listId = fileDataManager.nextListIdentifier(2, false);
		UUID uuid = UUID.fromString(listId.getName());
		assertEquals(4, uuid.version()); // random
	}

	@Override
	@Before
	public void setDataManager() {
		fileDataManager = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
		dManager = fileDataManager;
	}

}
