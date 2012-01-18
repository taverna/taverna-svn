package idaservicetype.idaservicetype;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class MyObject {

	private String exampleString;
	
	private String dataflow;

	private String selectedTask;
	
	boolean isTemplate;

	public void setExampleString(String exampleString) {
		this.exampleString = exampleString;
	}

	public String getExampleString() {
		return exampleString;
	}

	public void setDataflow(String dataflow) {
		this.dataflow = dataflow;
	}

	public String getDataflow() {
		return dataflow;
	}

	public void setSelectedTask(String selectedTask) {
		this.selectedTask = selectedTask;
	}

	public String getSelectedTask() {
		return selectedTask;
	}
	
}
