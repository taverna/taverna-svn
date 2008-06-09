package net.sf.taverna.t2.workbench.file;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public abstract class FileManager {

	private static FileManager instance;

	/**
	 * Get the {@link FileManager} implementation singleton as discovered
	 * through an {@link SPIRegistry}.
	 * 
	 * @throws IllegalStateException
	 *             If no implementation was found.
	 * @return Discovered {@link FileManager} implementation singleton.
	 */
	public static synchronized FileManager getInstance()
			throws IllegalStateException {
		if (instance == null) {
			SPIRegistry<FileManager> registry = new SPIRegistry<FileManager>(
					FileManager.class);
			try {
				instance = registry.getInstances().get(0);
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalStateException(
						"Could not find implementation of " + FileManager.class);
			}
		}
		return instance;
	}

	public abstract Dataflow openDataflow(URL dataflowURL) throws OpenException;

	public abstract List<Dataflow> getOpenDataflows();

	public abstract void closeCurrentDataflow(boolean ignoreUnsaved)
			throws UnsavedException;

	public abstract void closeDataflow(Dataflow dataflow, boolean ignoreUnsaved)
			throws UnsavedException;

	public abstract void saveCurrentDataflow(File dataflowFile)
			throws SaveException;

	public abstract void saveDataflow(Dataflow dataflow, File dataflowFile)
			throws SaveException;

	public abstract boolean isDataflowChanged(Dataflow dataflow);

}
