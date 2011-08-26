package net.sf.taverna.t2.activities.dataflow.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;


public class DataflowActivityItem extends AbstractActivityItem {

	@Override
	protected Object getConfigBean() {
		return EditsRegistry.getEdits().createDataflow();
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(DataflowActivityItem.class.getResource("/dataflow.png"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new DataflowActivity();
	}

	public String getType() {
		return "Workflow";
	}

	@Override
	public String toString() {
		return "Nested workflow";
	}
	
	
}
