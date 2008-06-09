package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.ModelMapConstants;
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

public class FileManagerImpl implements FileManager {

	private static Logger logger = Logger.getLogger(FileManagerImpl.class);
	private ModelMap modelMap = ModelMap.getInstance();
	private List<Dataflow> openDataflows = new ArrayList<Dataflow>();

	public Dataflow openDataflow(URL dataflowURL) throws OpenException {
		InputStream dummyWorkflowXMLstream;
		try {
			dummyWorkflowXMLstream = dataflowURL.openStream();
		} catch (IOException e) {
			throw new OpenException("Could not open " + dataflowURL, e);
		}
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
		Dataflow dataflow;
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

		modelMap.setModel(ModelMapConstants.CURRENT_DATAFLOW, dataflow);
		logger.info("Loaded workflow: " + dataflow.getLocalName() + " "
				+ dataflow.getInternalIdentier() + " from " + dataflowURL);
		openDataflows.add(dataflow);

		return dataflow;
	}

	public List<Dataflow> getOpenDataflows() {
		return new ArrayList<Dataflow>(openDataflows);
	}

	public void closeDataflow(Dataflow dataflow, boolean ignoreUnsaved)
			throws UnsavedException {
		openDataflows.remove(dataflow);
	}

	public void saveDataflow(Dataflow dataflow, File dataflowFile) throws SaveException {
		XMLSerializer serialiser = new XMLSerializerImpl();
		Element serialized;
		try {
			serialized = serialiser.serializeDataflow(dataflow);
		} catch (SerializationException e) {
			throw new SaveException("Could not serialize " + dataflow, e);
		}
		
	}

	public void closeCurrentDataflow(boolean ignoreUnsaved)
			throws UnsavedException {
		// TODO Auto-generated method stub
		
	}

	public void saveCurrentDataflow(File dataflowFile) throws SaveException {
		// TODO Auto-generated method stub
		
	}

}
