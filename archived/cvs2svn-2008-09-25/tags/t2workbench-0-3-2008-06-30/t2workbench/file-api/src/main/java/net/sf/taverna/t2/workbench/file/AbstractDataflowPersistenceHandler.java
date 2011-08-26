package net.sf.taverna.t2.workbench.file;

import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public abstract class AbstractDataflowPersistenceHandler implements
		DataflowPersistenceHandler {

	public List<FileType> getOpenFileTypes() {
		return Collections.emptyList();
	}

	public List<FileType> getSaveFileTypes() {
		return Collections.emptyList();
	}

	public List<Class<?>> getOpenSourceTypes() {
		return Collections.emptyList();
	}

	public List<Class<?>> getSaveDestinationTypes() {
		return Collections.emptyList();
	}

	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		throw new UnsupportedOperationException();
	}

	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException {
		throw new UnsupportedOperationException();
	}

	public boolean wouldOverwriteDataflow(Dataflow dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo) {
		throw new UnsupportedOperationException();
	}
}
