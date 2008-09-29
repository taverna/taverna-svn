package net.sf.taverna.t2.workbench.ui.actions.activity;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
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
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(configureActivityEdit);
			Processor p = findProcessor(currentDataflow);
			if (p!=null && p.getActivityList().size()==1) {
				editList.add(edits.getMapProcessorPortsForActivityEdit(p));
			}
			EditManager.getInstance().doDataflowEdit(currentDataflow, new CompoundEdit(editList));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			e.printStackTrace();
		}
	}
	
	protected Processor findProcessor(Dataflow df) {
		Activity<?> activity = getActivity();
		
		for (Processor processor : df.getProcessors()) {
			if (processor.getActivityList().contains(activity)) return processor;
		}
		return null;
	}
}
