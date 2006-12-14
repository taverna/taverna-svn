/*
 * FetaRegistryIndex.java
 *
 * Created on 04 December 2005, 18:18
 */

package uk.ac.man.cs.img.fetaEngine.store;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Pinar
 */
public class FetaPersistentRegistryIndex {
	private Set indexKeys;

	private String dumpFileName;

	private FetaRegistryIndexPersisterThread persister;

	/** Creates a new instance of FetaRegistryIndex */
	public FetaPersistentRegistryIndex(String persistanceFileLocation) {
		indexKeys = new HashSet();
		dumpFileName = persistanceFileLocation;
		persister = new FetaRegistryIndexPersisterThread(this);
		persister.start();

	}

	public void addIndexEntry(String operationURLStr) {
		indexKeys.add(operationURLStr);

	}

	public void removeIndexEntry(String operationURLStr) {
		indexKeys.remove(operationURLStr);

	}

	public Set getIndexKeys() {
		return indexKeys;
	}

	class FetaRegistryIndexPersisterThread extends Thread {

		FetaPersistentRegistryIndex regIndex;

		public FetaRegistryIndexPersisterThread(
				FetaPersistentRegistryIndex registryIndex) {
			this.regIndex = registryIndex;
		}

		public void run() {
			while (true) {

				try {
					System.out.println("writing out index to disk!!");
					File outputFile = new File(dumpFileName);
					FileWriter writer = new FileWriter(outputFile);
					for (Iterator it = this.regIndex.getIndexKeys().iterator(); it
							.hasNext();) {
						try {
							String operationURLStr = (String) it.next();
							writer.write(operationURLStr + "\n");

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					writer.close();

				} catch (Exception exp) {
					exp.printStackTrace();
				}
				try {
					Thread.sleep(100000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
					// ?? do what
				}
			}

		}
	}

}
