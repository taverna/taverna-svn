package net.sf.taverna.t2.workbench.ui.actions.activity;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

public abstract class ActivityConfigurationAction<A extends Activity<?>> extends AbstractAction {

	private static Logger logger = Logger
			.getLogger(ActivityConfigurationAction.class);
	
	private A activity;
	
	public ActivityConfigurationAction(A activity) {
		this.activity=activity;
	}
	
	protected A getActivity() {
		return activity;
	}
	
	protected void configureActivity(Object configurationBean) {
		Edits edits = EditsRegistry.getEdits();
		Edit<?> configureActivityEdit = edits.getConfigureActivityEdit(getActivity(),configurationBean);
		Dataflow currentDataflow = FileManager.getInstance().getCurrentDataflow();
		try {
			EditManager.getInstance().doDataflowEdit(currentDataflow, configureActivityEdit);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			e.printStackTrace();
		}
	}
}
