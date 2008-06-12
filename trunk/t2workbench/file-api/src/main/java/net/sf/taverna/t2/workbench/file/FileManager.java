package net.sf.taverna.t2.workbench.file;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workbench.file.FileManager.FileManagerEvent;

public abstract class FileManager implements Observable<FileManagerEvent> {

	public static abstract class FileManagerEvent {
	}

	public static abstract class AbstractDataflowEvent extends FileManagerEvent {
		private final Dataflow dataflow;

		public AbstractDataflowEvent(Dataflow dataflow) {
			this.dataflow = dataflow;
		}

		public Dataflow getDataflow() {
			return dataflow;
		}
	}

	public static class OpenedDataflowEvent extends AbstractDataflowEvent {

		public OpenedDataflowEvent(Dataflow dataflow) {
			super(dataflow);
		}
	}

	public static class ClosedDataflowEvent extends AbstractDataflowEvent {

		public ClosedDataflowEvent(Dataflow dataflow) {
			super(dataflow);
		}
	}

	public static class SavedDataflowEvent extends AbstractDataflowEvent {

		public SavedDataflowEvent(Dataflow dataflow) {
			super(dataflow);
		}
	}

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

	public abstract boolean canSaveCurrentWithoutFilename();

	public abstract boolean canSaveWithoutFilename(Dataflow dataflow);

	public abstract void closeCurrentDataflow(boolean failOnUnsaved)
			throws UnsavedException;

	public abstract void closeDataflow(Dataflow dataflow, boolean failOnUnsaved)
			throws UnsavedException;

	public abstract Dataflow getCurrentDataflow();

	public abstract List<Dataflow> getOpenDataflows();

	public abstract boolean isDataflowChanged(Dataflow dataflow);

	public abstract Dataflow newDataflow();

	public abstract void openDataflow(Dataflow dataflow);

	public abstract Dataflow openDataflow(InputStream workflowXMLstream)
			throws OpenException;

	public abstract Dataflow openDataflow(URL dataflowURL) throws OpenException;

	public abstract void saveCurrentDataflow(boolean failOnOverwrite)
			throws SaveException;

	public abstract void saveCurrentDataflow(File dataflowFile,
			boolean failOnOverwrite) throws SaveException;

	public abstract void saveDataflow(Dataflow dataflow, boolean failOnOverwrite)
			throws SaveException;

	public abstract void saveDataflow(Dataflow dataflow, File dataflowFile,
			boolean failOnOverwrite) throws SaveException;

	public abstract void setCurrentDataflow(Dataflow dataflow);

	public abstract void setDataflowChanged(Dataflow dataflow, boolean isChanged);

	public abstract File getCurrentDataflowFile();

	public abstract File getDataflowFile(Dataflow dataflow);

	public abstract URL getCurrentDataflowURL();

	public abstract URL getDataflowURL(Dataflow dataflow);

}
