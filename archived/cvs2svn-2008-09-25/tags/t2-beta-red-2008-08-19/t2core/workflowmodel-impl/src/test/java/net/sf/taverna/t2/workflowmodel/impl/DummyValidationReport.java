/**
 * 
 */
package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;

public class DummyValidationReport implements DataflowValidationReport {
	private final boolean valid;

	public DummyValidationReport(boolean valid) {
		this.valid = valid;
	}

	public boolean isValid() {
		return valid;
	}

	public List<? extends TokenProcessingEntity> getUnsatisfiedEntities() {
		return null;
	}

	public List<? extends DataflowOutputPort> getUnresolvedOutputs() {
		return null;
	}

	public List<? extends TokenProcessingEntity> getFailedEntities() {
		return null;
	}

	public Map<TokenProcessingEntity, DataflowValidationReport> getInvalidDataflows() {
		return null;
	}
}