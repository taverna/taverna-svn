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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.OpenException;
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
				 * FileOpeningTest#isChangedWithUndo().
				 */
				setDataflowChanged(dataflow, true);
			}
		}
	}

	private static Logger logger = Logger.getLogger(FileManagerImpl.class);
	private ModelMap modelMap = ModelMap.getInstance();
	private List<Dataflow> openDataflows = new ArrayList<Dataflow>();
	private EditManager editManager = EditManager.getInstance();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private Set<Dataflow> changedDataflows = new HashSet<Dataflow>();

	public FileManagerImpl() {
		editManager.addObserver(editManagerObserver);
	}

	public void setDataflowChanged(Dataflow dataflow, boolean isChanged) {
		if (isChanged) {
			changedDataflows.add(dataflow);
		} else {
			changedDataflows.remove(dataflow);
		}
	}

	public Dataflow openDataflow(URL dataflowURL) throws OpenException {
		InputStream dummyWorkflowXMLstream;
		try {
			dummyWorkflowXMLstream = dataflowURL.openStream();
		} catch (IOException e) {
			throw new OpenException("Could not open " + dataflowURL, e);
		}
		Dataflow dataflow;
		try {
			XMLDeserializer deserializer = new XMLDeserializerImpl();
			SAXBuilder builder = new SAXBuilder();
			Document document;
			try {
				document = builder.build(dummyWorkflowXMLstream);
			} catch (JDOMException e) {
				throw new OpenException("Could not parse XML of dataflow from "
						+ dataflowURL, e);
			} catch (IOException e) {
				throw new OpenException("Could not read dataflow from "
						+ dataflowURL, e);
			}

			try {
				dataflow = deserializer.deserializeDataflow(document
						.getRootElement());
			} catch (DeserializationException e) {
				throw new OpenException("Could not deserialise dataflow from "
						+ dataflowURL, e);
			} catch (EditException e) {
				throw new OpenException("Could not construct dataflow from "
						+ dataflowURL, e);
			}
		} finally {
			try {
				dummyWorkflowXMLstream.close();
			} catch (IOException e) {
				logger.warn("Could not close stream from " + dataflowURL, e);
			}
		}

		logger.info("Loaded workflow: " + dataflow.getLocalName() + " "
				+ dataflow.getInternalIdentier() + " from " + dataflowURL);

		openDataflows.add(dataflow);
		modelMap.setModel(ModelMapConstants.CURRENT_DATAFLOW, dataflow);

		return dataflow;
	}

	public List<Dataflow> getOpenDataflows() {
		return new ArrayList<Dataflow>(openDataflows);
	}

	public void closeDataflow(Dataflow dataflow, boolean ignoreUnsaved)
			throws UnsavedException {
		openDataflows.remove(dataflow);
	}

	public void saveDataflow(Dataflow dataflow, File dataflowFile)
			throws SaveException {
		XMLSerializer serialiser = new XMLSerializerImpl();
		Element serialized;
		try {
			serialized = serialiser.serializeDataflow(dataflow);
		} catch (SerializationException e) {
			throw new SaveException("Could not serialize " + dataflow, e);
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
		setDataflowChanged(dataflow, false);
	}

	public void closeCurrentDataflow(boolean ignoreUnsaved)
			throws UnsavedException {
		Dataflow dataflow = (Dataflow) modelMap
				.getModel(ModelMapConstants.CURRENT_DATAFLOW);
		if (dataflow != null) {
			closeDataflow(dataflow, ignoreUnsaved);
		}
	}

	public void saveCurrentDataflow(File dataflowFile) throws SaveException {
		Dataflow dataflow = (Dataflow) modelMap
				.getModel(ModelMapConstants.CURRENT_DATAFLOW);
		if (dataflow != null) {
			saveDataflow(dataflow, dataflowFile);
		}
	}

	public boolean isDataflowChanged(Dataflow dataflow) {
		return changedDataflows.contains(dataflow);
	}
}
