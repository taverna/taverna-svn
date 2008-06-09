package net.sf.taverna.t2.workbench.file;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface FileManager {

	public Dataflow openDataflow(URL dataflowURL) throws OpenException ;

	public List<Dataflow> getOpenDataflows();

	public void closeCurrentDataflow(boolean ignoreUnsaved) throws UnsavedException;
	
	public void closeDataflow(Dataflow dataflow, boolean ignoreUnsaved) throws UnsavedException;

	public void saveCurrentDataflow(File dataflowFile) throws SaveException;

	public void saveDataflow(Dataflow dataflow, File dataflowFile) throws SaveException;

	
}
