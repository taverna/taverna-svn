package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.io.File;

 import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class OpenNestedWorkflowAction extends OpenWorkflowAction{

	private static final long serialVersionUID = -5398423684000142379L;

	private static Logger logger = Logger.getLogger(OpenNestedWorkflowAction.class);

	private FileManager fileManager = FileManager.getInstance();
	
	public OpenNestedWorkflowAction(){
		super();
	}
	
	/**
	 * Opens a nested workflow from a file (should be one file even though
	 * the method takes a list of files - this is because it overrides the 
	 * #{@link net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction.java#openWorkflows(Component, File[], FileType, OpenCallback)}).
	 */
	@Override
	public void openWorkflows(final Component parentComponent, File[] files,
			FileType fileType, OpenCallback openCallback) {
		
		ErrorLoggingOpenCallbackWrapper callback = new ErrorLoggingOpenCallbackWrapper(
				openCallback);
		for (final File file : files) {

			try {
				callback.aboutToOpenDataflow(file);
				Dataflow dataflow = fileManager.openDataflow(fileType, file);
				callback.openedDataflow(file, dataflow);
			} catch (final RuntimeException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex)) {
					showErrorMessage(parentComponent, file, ex);
				}
			} catch (final OpenException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex)) {
					showErrorMessage(parentComponent, file, ex);
				}
				return;
			}
		}
	}
}
