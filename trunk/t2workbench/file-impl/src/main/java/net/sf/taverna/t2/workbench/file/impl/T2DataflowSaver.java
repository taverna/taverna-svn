package net.sf.taverna.t2.workbench.file.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class T2DataflowSaver extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {

	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
	private static Logger logger = Logger.getLogger(T2DataflowSaver.class);

	@Override
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException {
		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		OutputStream outStream;
		if (destination instanceof File) {
			try {
				outStream = new FileOutputStream((File) destination);
			} catch (FileNotFoundException e) {
				throw new SaveException("Can't create dataflow file "
						+ destination, e);
			}
		} else if (destination instanceof OutputStream) {
			outStream = (OutputStream) destination;
		} else {
			throw new IllegalArgumentException("Unsupported destination type "
					+ destination.getClass());
		}
		try {
			saveDataflowToStream(dataflow, outStream);
		} finally {
			if (!(destination instanceof OutputStream)) {
				// Only close if we opened the stream
				try {
					outStream.close();
				} catch (IOException e) {
					logger.warn("Could not close stream", e);
				}
			}
		}

		if (destination instanceof File) {
			return new FileDataflowInfo(T2_FLOW_FILE_TYPE, (File) destination,
					dataflow);
		}
		return new DataflowInfo(T2_FLOW_FILE_TYPE, destination, dataflow);

	}

	protected void saveDataflowToStream(Dataflow dataflow,
			OutputStream fileOutStream) throws SaveException {
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				fileOutStream);
		XMLOutputter outputter = new XMLOutputter();

		XMLSerializer serialiser = new XMLSerializerImpl();
		Element serialized;
		try {
			serialized = serialiser.serializeDataflow(dataflow);
		} catch (SerializationException e) {
			throw new SaveException("Could not serialize " + dataflow, e);
		}

		try {
			outputter.output(serialized, bufferedOutStream);
			bufferedOutStream.flush();
		} catch (IOException e) {
			throw new SaveException("Can't write dataflow", e);
		}
	}

	@Override
	public List<FileType> getSaveFileTypes() {
		return Arrays.<FileType> asList(T2_FLOW_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Arrays.<Class<?>> asList(File.class, OutputStream.class);
	}

	@Override
	public boolean wouldOverwriteDataflow(Dataflow dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo) {
		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(destination instanceof File)) {
			return false;
		}

		File file;
		try {
			file = ((File) destination).getCanonicalFile();
		} catch (IOException e) {
			return false;
		}
		if (!file.exists()) {
			return false;
		}

		Object lastDestination = lastDataflowInfo.getCanonicalSource();
		if (!(lastDestination instanceof File)) {
			return true;
		}
		File lastFile = (File) lastDestination;
		if (! lastFile.getAbsoluteFile().equals(file)) {
			return true;
		}
		
		
		Date lastModified = new Date(file.lastModified());
		if (lastModified.equals(lastDataflowInfo.getLastModified())) {
			return false;
		}
		return true;
	}

}
