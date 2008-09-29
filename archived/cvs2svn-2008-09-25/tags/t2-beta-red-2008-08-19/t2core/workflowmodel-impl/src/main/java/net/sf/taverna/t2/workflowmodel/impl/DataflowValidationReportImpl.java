package net.sf.taverna.t2.workflowmodel.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;

/**
 * Simple implementation of the DataflowValidationReport interface
 * 
 * @author Tom Oinn
 * 
 */
public class DataflowValidationReportImpl implements DataflowValidationReport {

	private final List<TokenProcessingEntity> failed;
	private final Map<TokenProcessingEntity, DataflowValidationReport> invalidDataflows;
	private final List<DataflowOutputPort> unresolvedOutputs;
	private final List<TokenProcessingEntity> unsatisfied;
	private boolean valid;

	DataflowValidationReportImpl(boolean isValid,
			List<TokenProcessingEntity> failedProcessors,
			List<TokenProcessingEntity> unsatisfiedProcessors,
			List<DataflowOutputPort> unresolvedOutputs, Map<TokenProcessingEntity, DataflowValidationReport> invalidDataflows) {
		this.valid = isValid;
		this.invalidDataflows = Collections.unmodifiableMap(invalidDataflows);
		this.failed = Collections.unmodifiableList(failedProcessors);
		this.unsatisfied = Collections.unmodifiableList(unsatisfiedProcessors);
		this.unresolvedOutputs = Collections.unmodifiableList(unresolvedOutputs);
	}

	public List<? extends TokenProcessingEntity> getFailedEntities() {
		return failed;
	}

	public Map<TokenProcessingEntity, DataflowValidationReport> getInvalidDataflows() {
		return invalidDataflows;
	}

	public List<? extends DataflowOutputPort> getUnresolvedOutputs() {
		return unresolvedOutputs;
	}
	
	public List<? extends TokenProcessingEntity> getUnsatisfiedEntities() {
		return unsatisfied;
	}

	public boolean isValid() {
		return valid;
	}

}
