package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology;
/**
 * Java constants for classes and predicates in provenance.owl.
 * Automatically generated from provenance.owl using provenance.xsl.
 */
public class ProvenanceOntologyConstants { 

	public static final String NS = "http://www.mygrid.org.uk/provenance#"; 

/**
 * Java constants for classes in provenance.owl.
 * Automatically generated from provenance.owl using provenance.xsl.
 */
	public static class Classes { 

		public static final String FAILEDPROCESSRUN = NS + "FailedProcessRun";
				
		public static final String FAILEDRUN = NS + "FailedRun";
				
		public static final String PROCESSRUN = NS + "ProcessRun";
				
		public static final String PROCESSITERATION = NS + "ProcessIteration";
				
		public static final String NESTEDWORKFLOWRUN = NS + "NestedWorkflowRun";
				
		public static final String WORKFLOWRUN = NS + "WorkflowRun";
				
		public static final String RUN = NS + "Run";
				
		public static final String PROCESS = NS + "Process";
				
		public static final String RUNNABLE = NS + "Runnable";
				
		public static final String PROCESSPROPERTY = NS + "ProcessProperty";
				
		public static final String FAILEDNESTEDWORKFLOWRUN = NS + "FailedNestedWorkflowRun";
				
		public static final String FAILEDWORKFLOWRUN = NS + "FailedWorkflowRun";
				
		public static final String ATOMICDATA = NS + "AtomicData";
				
		public static final String DATAOBJECT = NS + "DataObject";
				
		public static final String CHANGEDDATAOBJECT = NS + "ChangedDataObject";
				
		public static final String EXPERIMENTER = NS + "Experimenter";
				
		public static final String WORKFLOW = NS + "Workflow";
				
		public static final String INPUTDATANAME = NS + "InputDataName";
				
		public static final String DATANAME = NS + "DataName";
				
		public static final String OUTPUTDATANAME = NS + "OutputDataName";
				
		public static final String NESTEDWORKFLOWPROCESSRUN = NS + "NestedWorkflowProcessRun";
				
		public static final String DATACOLLECTION = NS + "DataCollection";
				
		public static final String ORGANIZATION = NS + "Organization";
				
		public static final String PROCESSRUNWITHITERATIONS = NS + "ProcessRunWithIterations";
				
	}

/**
 * Java constants for object properties in provenance.owl.
 * Automatically generated from provenance.owl using provenance.xsl.
 */
	public static class ObjectProperties { 

		public static final String BELONGSTO = NS + "belongsTo";
				
		public static final String DATADERIVEDFROM = NS + "dataDerivedFrom";
				
		public static final String PROCESSINPUT = NS + "processInput";
				
		public static final String RUNS = NS + "runs";
				
		public static final String INPUTDATAHASNAME = NS + "inputDataHasName";
				
		public static final String DATAHASNAME = NS + "dataHasName";
				
		public static final String NESTEDWORKFLOW = NS + "nestedWorkflow";
				
		public static final String RUNSWORKFLOW = NS + "runsWorkflow";
				
		public static final String HASPROPERTY = NS + "hasProperty";
				
		public static final String USERPREDICATE = NS + "userPredicate";
				
		public static final String EXECUTEDPROCESSRUN = NS + "executedProcessRun";
				
		public static final String NESTEDRUN = NS + "nestedRun";
				
		public static final String RUNSPROCESS = NS + "runsProcess";
				
		public static final String LAUNCHEDBY = NS + "launchedBy";
				
		public static final String WORKFLOWOUTPUT = NS + "workflowOutput";
				
		public static final String ITERATION = NS + "iteration";
				
		public static final String CONTAINSDATA = NS + "containsData";
				
		public static final String DATAWRAPPEDINTO = NS + "dataWrappedInto";
				
		public static final String OLDDATAOBJECT = NS + "oldDataObject";
				
		public static final String WORKFLOWINPUT = NS + "workflowInput";
				
		public static final String PROCESSOUTPUT = NS + "processOutput";
				
		public static final String OUTPUTDATAHASNAME = NS + "outputDataHasName";
				
	}

/**
 * Java constants for datatype properties in provenance.owl.
 * Automatically generated from provenance.owl using provenance.xsl.
 */
	public static class DatatypeProperties { 

		public static final String WORKFLOWAUTHOR = NS + "workflowAuthor";
				
		public static final String NUMBEROFITERATIONS = NS + "numberOfIterations";
				
		public static final String WORKFLOWDESCRIPTION = NS + "workflowDescription";
				
		public static final String VALUE = NS + "value";
				
		public static final String DATASYNTACTICTYPE = NS + "dataSyntacticType";
				
		public static final String MIMETYPE = NS + "mimeType";
				
		public static final String ENDTIME = NS + "endTime";
				
		public static final String STARTTIME = NS + "startTime";
				
		public static final String CAUSE = NS + "cause";
				
	}
}