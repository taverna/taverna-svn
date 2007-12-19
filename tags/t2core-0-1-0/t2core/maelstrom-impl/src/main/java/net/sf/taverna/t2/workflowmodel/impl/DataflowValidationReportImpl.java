package net.sf.taverna.t2.workflowmodel.impl;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Simple implementation of the DataflowValidationReport interface
 * 
 * @author Tom Oinn
 * 
 */
public class DataflowValidationReportImpl implements DataflowValidationReport {

	private List<Processor> failed = new ArrayList<Processor>();
	private List<Processor> unsatisfied = new ArrayList<Processor>();
	private List<DataflowOutputPort> unresolvedOutputs = new ArrayList<DataflowOutputPort>();
	private boolean valid;

	DataflowValidationReportImpl(boolean isValid,
			List<Processor> failedProcessors,
			List<Processor> unsatisfiedProcessors,
			List<DataflowOutputPort> unresolvedOutputs) {
		this.valid = isValid;
		this.failed = Collections.unmodifiableList(failedProcessors);
		this.unsatisfied = Collections.unmodifiableList(unsatisfiedProcessors);
		this.unresolvedOutputs = Collections.unmodifiableList(unresolvedOutputs);
	}

	public List<? extends Processor> getFailedProcessors() {
		return this.failed;
	}

	public List<? extends Processor> getUnsatisfiedProcessors() {
		return this.unsatisfied;
	}

	public List<? extends DataflowOutputPort> getUnresolvedOutputs() {
		return this.unresolvedOutputs;
	}
	
	public boolean isValid() {
		return this.valid;
	}

}
