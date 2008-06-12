package net.sf.taverna.t2.workbench.file.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.OpenException;
import net.sf.taverna.t2.workbench.file.OverwriteException;
import net.sf.taverna.t2.workbench.file.SaveException;
import net.sf.taverna.t2.workbench.file.UnsavedException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class FileManagerImpl extends FileManager {

	static Logger logger = Logger.getLogger(FileManagerImpl.class);

	/**
	 * The last blank dataflow created using #newDataflow() until it has been //
	 * changed - when this variable will be set to null again. Used to //
	 * automatically close unmodified blank dataflows on open
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
	public boolean canSaveCurrentWithoutFilename() {
		return canSaveWithoutFilename(getCurrentDataflow());
	}

	@Override
	public boolean canSaveWithoutFilename(Dataflow dataflow) {
		OpenDataflowInfo dataflowInfo = getOpenDataflowInfo(dataflow);
		return dataflowInfo.getFile() != null;
	}

	@Override
	public void closeCurrentDataflow(boolean failOnUnsaved)
			throws UnsavedException {
		Dataflow dataflow = getCurrentDataflow();
		if (dataflow != null) {
			closeDataflow(dataflow, failOnUnsaved);
		}
	}

	@Override
	public void closeDataflow(Dataflow dataflow, boolean failOnUnsaved)
			throws UnsavedException {
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

	public List<Observer<FileManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	@Override
	public List<Dataflow> getOpenDataflows() {
		return new ArrayList<Dataflow>(openDataflowInfos.keySet());
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
	public Dataflow openDataflow(InputStream workflowXMLstream)
			throws OpenException {
		Dataflow dataflow = openDataflowInternal(workflowXMLstream);
		observers.notify(new OpenedDataflowEvent(dataflow));
		return dataflow;
	}

	@Override
	public Dataflow openDataflow(URL dataflowURL) throws OpenException {
		InputStream inputStream;
		try {
			inputStream = dataflowURL.openStream();
		} catch (IOException e) {
			throw new OpenException("Could not open " + dataflowURL, e);
		}
		logger.debug("Loading dataflow from " + dataflowURL);
		Dataflow dataflow;
		try {
			dataflow = openDataflowInternal(inputStream);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.warn("Could not close stream ", e);
			}
		}
		logger.info("Loaded workflow: " + dataflow.getLocalName() + " "
				+ dataflow.getInternalIdentier() + " from " + dataflowURL);
		getOpenDataflowInfo(dataflow).setOpenedFrom(dataflowURL);
		observers.notify(new OpenedDataflowEvent(dataflow));
		return dataflow;
	}

	public void removeObserver(Observer<FileManagerEvent> observer) {
		observers.removeObserver(observer);
	}

	@Override
	public void saveCurrentDataflow(boolean failOnOverwrite)
			throws SaveException {
		Dataflow dataflow = getCurrentDataflow();
		if (dataflow != null) {
			saveDataflow(dataflow, failOnOverwrite);
		}
	}

	@Override
	public void saveCurrentDataflow(File dataflowFile, boolean failOnOverwrite)
			throws SaveException {
		Dataflow dataflow = getCurrentDataflow();
		if (dataflow != null) {
			saveDataflow(dataflow, dataflowFile, failOnOverwrite);
		}
	}

	@Override
	public void saveDataflow(Dataflow dataflow, boolean failOnOverwrite)
			throws SaveException {
		OpenDataflowInfo lastSave = getOpenDataflowInfo(dataflow);
		if (lastSave.getFile() == null) {
			throw new SaveException("Can't save without filename " + dataflow);
		}
		saveDataflow(dataflow, lastSave.getFile(), failOnOverwrite);
	}

	@Override
	public void saveDataflow(Dataflow dataflow, File dataflowFile,
			boolean failOnOverwrite) throws SaveException {

		if (failOnOverwrite && wouldOverwriteDataflow(dataflow, dataflowFile)) {
			throw new OverwriteException(dataflowFile);
		}

		FileOutputStream fileOutStream;
		try {
			fileOutStream = new FileOutputStream(dataflowFile);
		} catch (FileNotFoundException e) {
			throw new SaveException("Can't create dataflow file "
					+ dataflowFile, e);
		}
		OutputStream outStream = new BufferedOutputStream(fileOutStream);
		XMLOutputter outputter = new XMLOutputter();

		XMLSerializer serialiser = new XMLSerializerImpl();
		Element serialized;
		try {
			serialized = serialiser.serializeDataflow(dataflow);
		} catch (SerializationException e) {
			throw new SaveException("Could not serialize " + dataflow, e);
		}

		try {
			outputter.output(serialized, outStream);
			outStream.close();
		} catch (IOException e) {
			throw new SaveException("Can't write dataflow to file "
					+ dataflowFile, e);
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
				logger.warn("Could not close stream to " + dataflowFile, e);
			}
		}
		getOpenDataflowInfo(dataflow).setSavedTo(dataflowFile);

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
		getOpenDataflowInfo(dataflow).setChanged(isChanged);
		if (blankDataflow == dataflow) {
			blankDataflow = null;
		}
	}

	protected synchronized OpenDataflowInfo getOpenDataflowInfo(
			Dataflow dataflow) {
		OpenDataflowInfo info = openDataflowInfos.get(dataflow);
		if (info != null) {
			return info;
		} else {
			throw new IllegalArgumentException("Dataflow was not opened"
					+ dataflow);
		}
	}

	protected Dataflow loadDataflowFromStream(InputStream workflowXMLstream)
			throws OpenException {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(workflowXMLstream);
		} catch (JDOMException e) {
			throw new OpenException("Could not parse XML of dataflow", e);
		} catch (IOException e) {
			throw new OpenException("Could not read dataflow", e);
		}

		Dataflow dataflow;
		try {
			dataflow = deserializer.deserializeDataflow(document
					.getRootElement());
		} catch (DeserializationException e) {
			throw new OpenException("Could not deserialise dataflow ", e);
		} catch (EditException e) {
			throw new OpenException("Could not construct dataflow ", e);
		}
		return dataflow;
	}

	protected void openDataflowInternal(Dataflow dataflow) {
		if (isDataflowOpen(dataflow)) {
			throw new IllegalArgumentException("Dataflow is already open: "
					+ dataflow);
		}
		openDataflowInfos.put(dataflow, new OpenDataflowInfo());
		setCurrentDataflow(dataflow);
		if (openDataflowInfos.size() == 2 && blankDataflow != null) {
			// Behave like a word processor and close the blank workflow
			// when another one has been opened
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
		Dataflow dataflow = loadDataflowFromStream(workflowXMLstream);
		openDataflowInternal(dataflow);
		return dataflow;
	}

	protected boolean wouldOverwriteDataflow(Dataflow dataflow,
			File dataflowFile) throws OverwriteException {
		if (!dataflowFile.exists()) {
			return false;
		}
		OpenDataflowInfo dataflowInfo = getOpenDataflowInfo(dataflow);
		synchronized (dataflowInfo) {
			File lastSavedFile = dataflowInfo.getFile();
			if (lastSavedFile == null) {
				return true;
			}
			try {
				dataflowFile = dataflowFile.getCanonicalFile();
			} catch (IOException e1) {
				logger.warn("Could not canonically resolve " + dataflowFile);
				// Better play safe
				return true;
			}
			if (!lastSavedFile.equals(dataflowFile)) {
				return true;
			}
			// It's the same file, compare time stamps
			return (dataflowInfo.getLastModified() != dataflowFile
					.lastModified());
		}
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
					ModelMapConstants.CURRENT_PERSPECTIVE)) {
				Dataflow newModel = (Dataflow) message.getNewModel();
				if (!isDataflowOpen(newModel)) {
					openDataflowInternal(newModel);
				}
			}

		}
	}

	@Override
	public File getCurrentDataflowFile() {
		return getDataflowFile(getCurrentDataflow());
	}

	@Override
	public URL getCurrentDataflowURL() {
		return getDataflowURL(getCurrentDataflow());
	}

	@Override
	public File getDataflowFile(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).getFile();
	}

	@Override
	public URL getDataflowURL(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).getURL();
	}

}
