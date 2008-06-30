package net.sf.taverna.t2.workbench.ui.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Manages the mapping between Dataflows and DataflowSelectionModels.
 * 
 * @author David Withers
 */
public class DataflowSelectionManager {

	private Map<Dataflow, DataflowSelectionModel> dataflowSelectionModelMap = new HashMap<Dataflow, DataflowSelectionModel>();
	
	private static final DataflowSelectionManager instance = new DataflowSelectionManager();
	
	/**
	 * Private constructor, use DataflowSelectionManager.getInstance().
	 *
	 */
	private DataflowSelectionManager() {
	}
	
	/**
	 * Returns a singleton instance of a <code>DataflowSelectionManager</code>.
	 * 
	 * @return a singleton instance of a <code>DataflowSelectionManager</code>
	 */
	public static DataflowSelectionManager getInstance() {
		return instance;
	}
	
	/**
	 * Returns the <code>DataflowSelectionModel</code> for the dataflow.
	 * 
	 * @param dataflow
	 * @return the <code>DataflowSelectionModel</code> for the dataflow
	 */
	public DataflowSelectionModel getDataflowSelectionModel(Dataflow dataflow) {
		if (!dataflowSelectionModelMap.containsKey(dataflow)) {
			dataflowSelectionModelMap.put(dataflow, new DataflowSelectionModelImpl());
		}
		return dataflowSelectionModelMap.get(dataflow);
	}
	
}
