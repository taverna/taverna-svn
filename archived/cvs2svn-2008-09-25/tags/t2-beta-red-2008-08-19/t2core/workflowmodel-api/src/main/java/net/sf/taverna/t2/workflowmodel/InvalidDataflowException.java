package net.sf.taverna.t2.workflowmodel;


/**
 * Thrown if attempting to use a workflow that
 * is not {@link Dataflow#checkValidity() valid}.
 * <p>
 * The {@link DataflowValidationReport} can be retrieved using
 * {@link #getDataflowValidationReport()} and will provide details on how the
 * dataflow is invalid. The {@link #getDataflow()} will provide the invalid
 * dataflow.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class InvalidDataflowException extends Exception {

	private final DataflowValidationReport report;
	private final Dataflow dataflow;

	public InvalidDataflowException(Dataflow dataflow,
			DataflowValidationReport report) {
		this.report = report;
		this.dataflow = dataflow;
	}

	/**
	 * Get the {@link DataflowValidationReport validation report} for the
	 * failing dataflow.
	 * 
	 * @return Dataflow validation report
	 */
	public DataflowValidationReport getDataflowValidationReport() {
		return report;
	}

	/**
	 * Get the {@link Dataflow} that is not valid.
	 * 
	 * @return Invalid Dataflow
	 */
	public Dataflow getDataflow() {
		return dataflow;
	}

}
