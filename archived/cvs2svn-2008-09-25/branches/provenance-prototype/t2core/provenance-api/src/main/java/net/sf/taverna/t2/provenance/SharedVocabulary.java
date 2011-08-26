/**
 * 
 */
package net.sf.taverna.t2.provenance;

/**
 * @author paolo
 *
 */
public interface SharedVocabulary {

	/**
	 * basic types
	 */
	public static String DATAFLOW_EVENT_TYPE = "workflow";
	public static String PROCESS_EVENT_TYPE = "process";
	
	/**
	 * correspond to each type in net.sf.taverna.t2.provenance
	 */
	public static String PROVENANCE_EVENT_TYPE = "provenanceEvent";
	public static String ACTIVITY_EVENT_TYPE = "activity";
	public static String DATA_EVENT_TYPE = "data";
	public static String ERROR_EVENT_TYPE = "error";
	public static String INMEMORY_EVENT_TYPE = "inmemory";
	public static String INPUTDATA_EVENT_TYPE = "inputdata";
	public static String ITERATION_EVENT_TYPE = "iteration";
	public static String OUTPUTDATA_EVENT_TYPE = "outputdata";
	public static String PROCESSOR_EVENT_TYPE = "processor";
	public static String WEBSERVICE_EVENT_TYPE = "webservice";
	public static String WORKFLOW_EVENT_TYPE = "workflow";
	public static String END_WORKFLOW_EVENT_TYPE = "EOW";
	
	
	
}
