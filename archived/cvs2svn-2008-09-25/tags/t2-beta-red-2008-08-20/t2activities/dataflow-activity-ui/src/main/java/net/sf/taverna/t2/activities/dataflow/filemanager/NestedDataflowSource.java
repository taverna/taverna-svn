/**
 * 
 */
package net.sf.taverna.t2.activities.dataflow.filemanager;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * A source description for a nested dataflow, opened from a
 * {@link DataflowActivity} within an a {@link Processor} which is in the parent
 * {@link Dataflow}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class NestedDataflowSource {

	private final DataflowActivity dataflowActivity;

	private final Dataflow parentDataflow;

	public NestedDataflowSource(Dataflow parentDataflow,
			DataflowActivity dataflowActivity) {
		this.parentDataflow = parentDataflow;
		this.dataflowActivity = dataflowActivity;
	}

	public DataflowActivity getDataflowActivity() {
		return dataflowActivity;
	}

	public Dataflow getParentDataflow() {
		return parentDataflow;
	}
}