package net.sf.taverna.t2.workbench.file;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Manager of open files (Dataflows) in the workbench.
 * <p>
 * A {@link Dataflow} can be opened for the workbench using
 * {@link #openDataflow(URL)}, {@link #openDataflow(InputStream)} or
 * {@link #openDataflow(Dataflow)}. {@link Observer}s of the FileManager gets
 * notified with an {@link OpenedDataflowEvent}. The opened workflow is also
 * {@link #setCurrentDataflow(Dataflow) made the current dataflow}, available
 * through {@link #getCurrentDataflow()} or by observing the
 * {@link net.sf.taverna.t2.lang.ui.ModelMap} for the model name
 * {@link net.sf.taverna.t2.workbench.ModelMapConstants#CURRENT_DATAFLOW}.
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
 * and {@link #canSaveWithoutDestination(Dataflow)}. You can get the last
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
	 * True if {@link #saveDataflow(Dataflow, boolean)} can save the workflow,
	 * ie. if {@link #getDataflowFile(Dataflow)} is not <code>null</code>.
	 * 
	 * @see #saveDataflow(Dataflow, boolean)
	 * @param dataflow
	 *            The dataflow to check
	 * @return <code>true</code> if the given dataflow can be saved without
	 *         providing a filename
	 */
	public abstract boolean canSaveWithoutDestination(Dataflow dataflow);

	/**
	 * Close the specified dataflow.
	 * <p>
	 * A closed dataflow can no longer be used with the save methods, and will
	 * disappear from the UI's list of open dataflows.
	 * </p>
	 * <p>
	 * If no more dataflows would be open after the close, a new, empty dataflow
	 * is opened as through {@link #newDataflow()}.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} to close
	 * @param failOnUnsaved
	 *            If <code>true</code>, fail on unsaved changes
	 * @throws UnsavedException
	 *             If failOnUnsaved was <code>true</code> and there has been
	 *             changes to the dataflow since the last save
	 */
	public abstract void closeDataflow(Dataflow dataflow, boolean failOnUnsaved)
			throws UnsavedException;

	/**
	 * Get the current dataflow as set through
	 * {@link #setCurrentDataflow(Dataflow)} or the
	 * {@link net.sf.taverna.t2.lang.ui.ModelMap} using the key
	 * {@link net.sf.taverna.t2.workbench.ModelMapConstants#CURRENT_DATAFLOW}.
	 * 
	 * @return The current dataflow, or <code>null</code> if no dataflow is
	 *         current
	 */
	public abstract Dataflow getCurrentDataflow();

	/**
	 * Get the last opened/saved {@link File} location for the given dataflow.
	 * <p>
	 * If the given dataflow's last opened/saved location was unknown or not a
	 * File (but say an HTTP-based URL), return <code>null</code>.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which file is to be returned
	 * @return The last opened/saved {@link File} for the given dataflow, or
	 *         <code>null</code> if unknown or not a file.
	 */
	public abstract Object getDataflowSource(Dataflow dataflow);

	public abstract Object getDataflowType(Dataflow dataflow);

	/**
	 * Get the list of currently open dataflows.
	 * 
	 * @return
	 */
	public abstract List<Dataflow> getOpenDataflows();

	public abstract boolean isDataflowChanged(Dataflow dataflow);

	public abstract Dataflow newDataflow();

	public abstract void openDataflow(Dataflow dataflow);

	public abstract Dataflow openDataflow(FileType fileType, Object source)
			throws OpenException;

	public abstract void saveDataflow(Dataflow dataflow, boolean failOnOverwrite)
			throws SaveException;

	public abstract void saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException;

	public abstract void setCurrentDataflow(Dataflow dataflow);

	public abstract void setCurrentDataflow(Dataflow dataflow, boolean openIfNeeded);
	
	public abstract void setDataflowChanged(Dataflow dataflow, boolean isChanged);

	public abstract List<FileFilter> getOpenFileFilters();

	public abstract List<FileFilter> getOpenFileFilters(Class<?> sourceClass);

	public abstract List<FileFilter> getSaveFileFilters(
			Class<?> destinationClass);

	public abstract List<FileFilter> getSaveFileFilters();

}
