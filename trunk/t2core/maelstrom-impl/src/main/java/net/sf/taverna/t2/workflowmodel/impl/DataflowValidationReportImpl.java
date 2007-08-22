package net.sf.taverna.t2.workflowmodel.impl;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Simple implementation of the DataflowValidationReport interface
 * 
 * @author Tom Oinn
 * 
 */
public class DataflowValidationReportImpl implements DataflowValidationReport {

	private List<ProcessorImpl> failed = new ArrayList<ProcessorImpl>();
	private List<ProcessorImpl> unsatisfied = new ArrayList<ProcessorImpl>();
	private boolean valid;

	DataflowValidationReportImpl(boolean isValid,
			List<ProcessorImpl> failedProcessors,
			List<ProcessorImpl> unsatisfiedProcessors) {
		this.valid = isValid;
		this.failed = Collections.unmodifiableList(failedProcessors);
		this.unsatisfied = Collections.unmodifiableList(unsatisfiedProcessors);
	}

	public List<? extends Processor> getFailedProcessors() {
		return this.failed;
	}

	public List<? extends Processor> getUnsatisfiedProcessors() {
		return this.unsatisfied;
	}

	public boolean isValid() {
		return this.valid;
	}

}
