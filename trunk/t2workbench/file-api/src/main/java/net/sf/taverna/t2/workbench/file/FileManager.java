package net.sf.taverna.t2.workbench.file;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;

/**
 * Manager of open files (ie. Dataflows) in the workbench.
 * <p>
 * A {@link Dataflow} can be opened for the workbench using
 * {@link #openDataflow(URL)}, {@link #openDataflow(InputStream)} or
 * {@link #openDataflow(Dataflow)}. {@link Observer}s of the FileManager gets
 * notified with an {@link OpenedDataflowEvent}. The opened workflow is also
 * {@link #setCurrentDataflow(Dataflow) made the current dataflow}, available
 * through {@link #getCurrentDataflow()} or by observing the {@link ModelMap}
 * for the model name {@link ModelMapConstants#CURRENT_DATAFLOW}.
 * </p>
 * <p>
 * A dataflow can be saved using {@link #saveDataflow(Dataflow, File, boolean)},
 * the current dataflow can be saved using
 * {@link #saveCurrentDataflow(File, boolean)}. Observers will be presented a
 * {@link SavedDataflowEvent}.
 * </p>
 * <p>
 * If the dataflow have been opened from a local {@link File} or saved to a
 * file, then {@link #saveCurrentDataflow(boolean)} and
 * {@link #saveDataflow(Dataflow, boolean)} will save to that file again. You
 * can check if this is the case using {@link #canSaveCurrentWithoutFilename()}
 * and {@link #canSaveWithoutFilename(Dataflow)}. You can get the last
 * saved/opened File or URL for a worklow using
 * {@link #getCurrentDataflowFile()}, {@link #getDataflowFile(Dataflow)},
 * {@link #getCurrentDataflowURL()} or {@link #getDataflowURL(Dataflow)}.
 * </p>
 * <p>
 * If the save methods are used with failOnOverwrite=true, an
 * {@link OverwriteException} will be thrown if the destination file already
 * exists and was not last written by a previous save on that dataflow. (This is
 * checked using timestamps on the file).
 * </p>
 * <p>
 * A dataflow can be closed using {@link #closeCurrentDataflow(boolean)} or
 * {@link #closeDataflow(Dataflow, boolean)}. A closed dataflow is no longer
 * monitored for changes and can no longer be used with the other operations,
 * except {@link #openDataflow(Dataflow)}.
 * </p>
 * <p>
 * If a dataflow has been changed using the {@link EditManager},
 * {@link #isDataflowChanged(Dataflow)} will return true until the next save. If
 * the close methods are used with failOnUnsaved=true, an
 * {@link UnsavedException} will be thrown if the dataflow has been changed.
 * </p>
 * 
 * <p>
 * {@link #getDataflowFile(Dataflow)}
 * </p>
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class FileManager implements Observable<FileManagerEvent> {

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

	/**
	 * True if {@link #saveCurrentDataflow(boolean)} can save the workflow, ie.
	 * if {@link #getCurrentDataflowFile()} is not <code>null</code>.
	 * <p>
	 * Note that first using this method and then
	 * {@link #saveCurrentDataflow(boolean)} is not thread safe as the current
	 * dataflow might be changed using {@link #setCurrentDataflow(Dataflow)} or
	 * the {@link ModelMap} meanwhile. If this is the case an
	 * {@link SaveException} might be thrown if the new current dataflow can't
	 * be saved.
	 * </p>
	 * 
	 * @see #canSaveWithoutFilename(Dataflow)
	 * @see #saveCurrentDataflow(boolean)
	 * 
	 * @return <code>true</code> if the current dataflow can be saved without
	 *         providing a filename
	 */
	public abstract boolean canSaveCurrentWithoutFilename();

	/**
	 * True if {@link #saveDataflow(Dataflow, boolean)} can save the workflow,
	 * ie. if {@link #getDataflowFile(Dataflow)} is not <code>null</code>.
	 * 
	 * @see #saveDataflow(Dataflow, boolean)
	 * @param dataflow
	 *            The dataflow to check
	 * @return <code>true</code> if the given dataflow can be saved without
	 *         providing a filename
	 */
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
