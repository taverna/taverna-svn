package net.sf.taverna.t2.workbench.file.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class FileManagerImpl extends FileManager {
	static Logger logger = Logger.getLogger(FileManagerImpl.class);

	/**
	 * The last blank dataflow created using #newDataflow() until it has been //
	 * changed - when this variable will be set to null again. Used to //
	 * automatically close unmodifieabout:blankd blank dataflows on open
	 */
	private Dataflow blankDataflow = null;

	private EditManager editManager = EditManager.getInstance();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private ModelMap modelMap = ModelMap.getInstance();
	private ModelMapObserver modelMapObserver = new ModelMapObserver();

	/**
	 * Ordered list of open dataflows
	 */
	private LinkedHashMap<Dataflow, OpenDataflowInfo> openDataflowInfos = new LinkedHashMap<Dataflow, OpenDataflowInfo>();

	private DataflowPersistenceHandlerRegistry persistanceHandlerRegistry = DataflowPersistenceHandlerRegistry
			.getInstance();

	protected MultiCaster<FileManagerEvent> observers = new MultiCaster<FileManagerEvent>(
			this);

	public FileManagerImpl() {
		editManager.addObserver(editManagerObserver);
		modelMap.addObserver(modelMapObserver);
	}

	public void addObserver(Observer<FileManagerEvent> observer) {
		observers.addObserver(observer);
	}

	@Override
	public boolean canSaveWithoutDestination(Dataflow dataflow) {
		OpenDataflowInfo dataflowInfo = getOpenDataflowInfo(dataflow);
		if (dataflowInfo.getSource() == null) {
			return false;
		}
		Set<DataflowPersistenceHandler> handlers = persistanceHandlerRegistry
				.getSaveHandlersForType(dataflowInfo.getFileType(),
						dataflowInfo.getDataflowInfo().getCanonicalSource().getClass());
		return !handlers.isEmpty();
	}

	@Override
	public void closeDataflow(Dataflow dataflow, boolean failOnUnsaved)
			throws UnsavedException {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		if (failOnUnsaved && getOpenDataflowInfo(dataflow).isChanged()) {
			throw new UnsavedException(dataflow);
		}
		if (dataflow.equals(getCurrentDataflow())) {
			// We'll need to change current dataflow
			// Find best candidate to the left or right
			List<Dataflow> dataflows = getOpenDataflows();
			int openIndex = dataflows.indexOf(dataflow);
			if (openIndex == -1) {
				throw new IllegalArgumentException("Dataflow was not opened"
						+ dataflow);
			} else if (openIndex > 0) {
				setCurrentDataflow(dataflows.get(openIndex - 1));
			} else if (openIndex == 0 && dataflows.size() > 1) {
				setCurrentDataflow(dataflows.get(1));
			} else {
				// If it was the last one, start a new, empty dataflow
				newDataflow();
			}
		}
		if (dataflow == blankDataflow) {
			blankDataflow = null;
		}
		openDataflowInfos.remove(dataflow);
		observers.notify(new ClosedDataflowEvent(dataflow));
	}

	@Override
	public Dataflow getCurrentDataflow() {
		return (Dataflow) modelMap.getModel(ModelMapConstants.CURRENT_DATAFLOW);
	}

	@Override
	public Object getDataflowSource(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).getSource();
	}

	@Override
	public Object getDataflowType(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).getFileType();
	}

	public List<Observer<FileManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	@Override
	public List<Dataflow> getOpenDataflows() {
		return new ArrayList<Dataflow>(openDataflowInfos.keySet());
	}

	@Override
	public List<FileFilter> getOpenFileFilters() {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : persistanceHandlerRegistry.getOpenFileTypes()) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	@Override
	public List<FileFilter> getOpenFileFilters(Class<?> sourceClass) {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : persistanceHandlerRegistry
				.getOpenFileTypesFor(sourceClass)) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	@Override
	public List<FileFilter> getSaveFileFilters() {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : persistanceHandlerRegistry.getSaveFileTypes()) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	@Override
	public List<FileFilter> getSaveFileFilters(Class<?> destinationClass) {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : persistanceHandlerRegistry
				.getSaveFileTypesFor(destinationClass)) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	@Override
	public boolean isDataflowChanged(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).isChanged();
	}

	public boolean isDataflowOpen(Dataflow dataflow) {
		return openDataflowInfos.containsKey(dataflow);
	}

	@Override
	public Dataflow newDataflow() {
		Dataflow dataflow = editManager.getEdits().createDataflow();
		blankDataflow = null;
		openDataflowInternal(dataflow);
		blankDataflow = dataflow;
		observers.notify(new OpenedDataflowEvent(dataflow));
		return dataflow;
	}

	@Override
	public void openDataflow(Dataflow dataflow) {
		openDataflowInternal(dataflow);
		observers.notify(new OpenedDataflowEvent(dataflow));
	}

	@Override
	public Dataflow openDataflow(FileType fileType, Object source)
			throws OpenException {
		Set<DataflowPersistenceHandler> handlers = persistanceHandlerRegistry
				.getOpenHandlersFor(fileType, source.getClass());
		if (handlers.isEmpty()) {
			throw new OpenException("Unsupported file type or class "
					+ fileType + " " + source.getClass());
		}
		OpenException lastException = null;
		for (DataflowPersistenceHandler handler : handlers) {
			try {
				DataflowInfo openDataflow = handler.openDataflow(fileType,
						source);
				Dataflow dataflow = openDataflow.getDataflow();
				logger.info("Loaded workflow: " + dataflow.getLocalName() + " "
						+ dataflow.getInternalIdentier() + " from " + source
						+ " using " + handler);
				openDataflowInternal(dataflow);
				getOpenDataflowInfo(dataflow).setOpenedFrom(openDataflow);
				observers.notify(new OpenedDataflowEvent(dataflow));
				return dataflow;
			} catch (OpenException ex) {
				logger.warn("Could not open from " + source + " using "
						+ handler);
				lastException = ex;
			}
		}
		throw new OpenException("Could not open " + source, lastException);
	}

	public void removeObserver(Observer<FileManagerEvent> observer) {
		observers.removeObserver(observer);
	}

	@Override
	public void saveDataflow(Dataflow dataflow, boolean failOnOverwrite)
			throws SaveException {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		OpenDataflowInfo lastSave = getOpenDataflowInfo(dataflow);
		if (lastSave.getSource() == null) {
			throw new SaveException("Can't save without source " + dataflow);
		}
		saveDataflow(dataflow, lastSave.getFileType(), lastSave.getSource(),
				failOnOverwrite);
	}

	@Override
	public void saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException {
		Set<DataflowPersistenceHandler> handlers = persistanceHandlerRegistry
				.getSaveHandlersForType(fileType, destination.getClass());
		if (handlers.isEmpty()) {
			throw new SaveException("Unsupported file type or class "
					+ fileType + " " + destination.getClass());
		}
		SaveException lastException = null;

		for (DataflowPersistenceHandler handler : handlers) {

			if (failOnOverwrite) {
				OpenDataflowInfo openDataflowInfo = getOpenDataflowInfo(dataflow);
				if (handler.wouldOverwriteDataflow(dataflow, fileType,
						destination, openDataflowInfo.getDataflowInfo())) {
					throw new OverwriteException(destination);
				}
			}
			try {
				DataflowInfo savedDataflow = handler.saveDataflow(dataflow,
						fileType, destination);
				savedDataflow.getDataflow();
				logger.info("Saved workflow: " + dataflow.getLocalName() + " "
						+ dataflow.getInternalIdentier() + " to "
						+ savedDataflow.getCanonicalSource() + " using "
						+ handler);
				getOpenDataflowInfo(dataflow).setSavedTo(savedDataflow);
				observers.notify(new SavedDataflowEvent(dataflow));
				return;
			} catch (SaveException ex) {
				logger.warn("Could not save to " + destination + " using "
						+ handler);
				lastException = ex;
			}
		}
		throw new SaveException("Could not save to " + destination,
				lastException);
	}

	@Override
	public void setCurrentDataflow(Dataflow dataflow) {
		if (!isDataflowOpen(dataflow)) {
			throw new IllegalArgumentException("Dataflow is not open: "
					+ dataflow);
		}
		modelMap.setModel(ModelMapConstants.CURRENT_DATAFLOW, dataflow);
	}

	@Override
	public void setDataflowChanged(Dataflow dataflow, boolean isChanged) {
		getOpenDataflowInfo(dataflow).setIsChanged(isChanged);
		if (blankDataflow == dataflow) {
			blankDataflow = null;
		}
	}

	protected synchronized OpenDataflowInfo getOpenDataflowInfo(
			Dataflow dataflow) {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		OpenDataflowInfo info = openDataflowInfos.get(dataflow);
		if (info != null) {
			return info;
		} else {
			throw new IllegalArgumentException("Dataflow was not opened"
					+ dataflow);
		}
	}

	protected void openDataflowInternal(Dataflow dataflow) {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}

		if (isDataflowOpen(dataflow)) {
			throw new IllegalArgumentException("Dataflow is already open: "
					+ dataflow);
		}
		openDataflowInfos.put(dataflow, new OpenDataflowInfo());
		setCurrentDataflow(dataflow);
		if (openDataflowInfos.size() == 2 && blankDataflow != null) {
			// Behave like a word processor and close the blank workflow
			// when another workflow has been opened
			try {
				closeDataflow(blankDataflow, true);
			} catch (UnsavedException e) {
				logger.error("Blank dataflow was modified "
						+ "and could not be closed");
			}
		}
	}

	protected Dataflow openDataflowInternal(InputStream workflowXMLstream)
			throws OpenException {
		Dataflow dataflow = new T2DataflowOpener()
				.openDataflowStream(workflowXMLstream);
		openDataflowInternal(dataflow);
		return dataflow;
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {

		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent) {
				AbstractDataflowEditEvent dataflowEdit = (AbstractDataflowEditEvent) message;
				Dataflow dataflow = dataflowEdit.getDataFlow();
				/**
				 * TODO: on undo/redo - keep last event or similar to determine
				 * if workflow was saved before. See
				 * FileManagerTest#isChangedWithUndo().
				 */
				setDataflowChanged(dataflow, true);
			}
		}
	}

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow newModel = (Dataflow) message.getNewModel();
				if (newModel != null) {
					if (!isDataflowOpen(newModel)) {
						openDataflowInternal(newModel);
					}
				}
				observers.notify(new SetCurrentDataflowEvent(newModel));
			}
		}
	}

}
