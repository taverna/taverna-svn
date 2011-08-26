package net.sf.taverna.t2.workbench.file;

import java.util.Collection;

import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface DataflowPersistenceHandler {

	public Collection<FileType> getOpenFileTypes();

	public Collection<Class<?>> getOpenSourceTypes();

	public Collection<Class<?>> getSaveDestinationTypes();

	public Collection<FileType> getSaveFileTypes();

	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException;

	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException;

	public boolean wouldOverwriteDataflow(Dataflow dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo);
}
